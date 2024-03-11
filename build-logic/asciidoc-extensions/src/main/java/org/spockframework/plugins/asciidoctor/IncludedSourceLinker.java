package org.spockframework.plugins.asciidoctor;

import org.asciidoctor.ast.*;
import org.asciidoctor.extension.*;
import org.asciidoctor.log.LogRecord;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readString;
import static java.util.stream.Collectors.joining;
import static org.asciidoctor.extension.Contexts.LISTING;
import static org.asciidoctor.extension.Contexts.LITERAL;
import static org.asciidoctor.log.Severity.WARN;

/**
 * This compound extension consists of an include processor, a tree processor, and a post processor.
 * It automatically links listing and literal blocks to the first included line (no matter whether it is
 * in the first included file or not) and logs a warning level message if a listing block with content but
 * without any included line is found as all code in the docs should be tested. It can also show an integration
 * with the Groovy web console for Groovy files in listing blocks, if the {@code gwc-base} document attribute is set.
 *
 * <p>The include processor is necessary as it is the only place where the path to the included file can be retrieved.
 * After an include directive was processed, it is like if the included lines were always in the file and there is
 * no way to even recognize the lines are included. Unfortunately, this also means that the complete needed include
 * logic needs to be reimplemented as there is currently no way to reuse the built-in functionality like tag-filtering.
 *
 * <p>We currently assume, that all includes that start with {@code ../} are including some file from the sources
 * and are done within a listing ({@code ----}) or literal ({@code ....}) block. This is important, as the include
 * processor works line-based and does not really have a viable chance to recognize where the included content lands.
 * And also cannot modify parts before the actual include directive, which is necessary for this.
 * Thus, the include processor can just plainly translate the include directive. It inserts a line with markers that
 * can be found and transformed later, that includes the link to the included file on GitHub, if any line was included.
 * After that, it does the usual include logic.
 *
 * <p>Currently, only the {@code tag}, {@code tags}, {@code lines}, and {@code indent} attributes on the include
 * directive are supported, but should do the exact same filtering as the Asciidoctor implementation. If it behaves
 * differently, this is a bug and never intentional and should be fixed here, unless it is an actual bug in the
 * Asciidoctor implementation. If additional attributes are needed like {@code encoding}, or {@code opts}, they
 * first need to be implemented here and currently throw an exception if used by someone used to them from other adoc
 * documents or the AsciiDoc documentation.
 *
 * <p>The next part of the implementation is the tree processor, which is called relatively late in the transformation
 * process, right before the actual conversion and then post processors are run, but after all other processors and
 * logic finished their work. The tree processor iterates through all listing and literal blocks.
 * <a href="https://discuss.asciidoctor.org/Difference-between-literal-and-listing-tp1830p1835.html">Literal
 * blocks are meant for output while listing blocks are meant for input or with {@code source} style for sources.</a>
 * If a literal block shows included content it is linked nevertheless, but it will never show a "Play" icon and there
 * will be no warning if there is no include. If a listing block shows content it is linked, and if it is a Groovy file
 * and the {@code gwc-base} document attribute is set, also a "Play" icon is shown to open the file in the Groovy
 * Web Console.
 *
 * <p>Finally, the post processor verifies in the end result, that there are no left-over marker lines.
 * This would for example happen if an include starting with {@code ../} is done outside a literal or listing block,
 * as the include processor does not know where it is and the tree processor only searches for those blocks.
 * So if such includes are necessary, the logic here has to be adapted too, to not end up with marker lines in the
 * resulting files.
 */
public class IncludedSourceLinker {
  /**
   * This padding is used as the start of the marker lines, because at the time the tree processor
   * runs, the indenting magic already happened, and if the marker line does not start with enough padding,
   * the code might not be unindented enough, or we would need to implement own indent-handling logic for
   * the including blocks.
   */
  private static final String INCLUDE_SOURCE_MARKER_PADDING =
    "                                                                                                    ";

  /**
   * A unique marker for the lines inserted by the include processor, then later
   * transformed and removed by the tree processor, and verified by the post processor.
   */
  private final String includeSourceMarker = UUID.randomUUID().toString();

  /**
   * The length of the include source marker for efficient substring usage.
   */
  private final int includeSourceMarkerLength = includeSourceMarker.length();

  public void register(JavaExtensionRegistry registry) {
    registry.includeProcessor(
      new IncludeProcessor() {
        /**
         * Pattern used to split a list of tags or line ranges, if they are separated by comma.
         * It is intentional that surrounding whitespace is not included.
         * This is for being consistent with the Asciidoctor built-in handling.
         * The surrounding whitespace is considered part of for example the tag name,
         * even if it will never match a tag then, as tag names must only consist of non-whitespace characters.
         */
        private static final Pattern COMMA_DELIMITER_PATTERN = Pattern.compile(",");

        /**
         * Pattern used to split a list of tags or line ranges, if they are separated by semicolon.
         * It is intentional that surrounding whitespace is not included.
         * This is for being consistent with the Asciidoctor built-in handling.
         * The surrounding whitespace is considered part of for example the tag name,
         * even if it will never match a tag then, as tag names must only consist of non-whitespace characters.
         */
        private static final Pattern SEMICOLON_DELIMITER_PATTERN = Pattern.compile(";");

        /**
         * Pattern to split a range into its bounds.
         */
        private static final Pattern SPLIT_RANGE_PATTERN = Pattern.compile("\\Q..\\E");

        /**
         * Pattern to extract an integer from a {@code String} like Ruby does it.
         */
        private static final Pattern EXTRACT_NUMBER_PATTERN = Pattern.compile("^[+-]?\\d++");

        /**
         * The pattern for matching a tagged line in the included file.
         * This is adapted from the original Ruby regex in Asciidoctor.
         *
         * @see <a href ="https://github.com/asciidoctor/asciidoctor/blob/4dec7ad055b30a5fe983d44019f9e5531a88d43a/lib/asciidoctor/rx.rb#L108">lib/asciidoctor/rx.rb</a>
         */
        private static final Pattern TAG_LINE_PATTERN = Pattern.compile("\\b(?<boundary>tag|end)::(?<tag>\\S+?)\\[](?: |$)");

        @Override
        public boolean handles(String target) {
          // we assume that every include that starts with `../` is happening inside a listing or literal block
          // and is pointing to a committed file in our repository, so we do the include logic in this processor
          // to get hold of the included filename for generating a link and eventually a play button
          return target.startsWith("../");
        }

        @Override
        public void process(Document document, PreprocessorReader reader, String target, Map<String, Object> attributes) {
          // verify that only supported attributes of the standard include directive are used
          verifyUsedAttributes(reader, attributes);

          // read the includee text if possible
          Path includee = Path.of(reader.getFile()).getParent().resolve(target);
          String includeeText = readText(includee);

          // algorithm for lines and tags filtering is adapted from original Asciidoctor implementation at
          // https://github.com/asciidoctor/asciidoctor/blob/4dec7ad055b30a5fe983d44019f9e5531a88d43a/lib/asciidoctor/reader.rb#L1057
          // hopefully once https://github.com/asciidoctor/asciidoctor/issues/571 is resolved we can reuse the built-in logic here
          if (attributes.containsKey("lines")) {
            filterForLines(document, reader, target, includee, includeeText, attributes);
          } else if (attributes.containsKey("tag") || attributes.containsKey("tags")) {
            filterForTags(document, reader, target, includee, includeeText, attributes);
          } else if (!includeeText.isEmpty()) {
            // include full includee
            pushMarkedInclude(document, reader, target, new StringBuilder(includeeText), 1, includee, attributes);
          }
        }

        /**
         * Filter the includee's text for the specified line ranges and push the result.
         *
         * @param document The currently rendered document
         * @param reader The current reader
         * @param target The target of the include directive, i.e. the included file starting with {@code ../}
         * @param includee The resolved includee
         * @param includeeText The text of the includee
         * @param attributes The attributes used on the include directive
         */
        private void filterForLines(Document document, PreprocessorReader reader, String target,
                                    Path includee, String includeeText, Map<String, Object> attributes) {
          List<Range> lineRanges = determineLineRangesToFilterFor(attributes);
          if (lineRanges == null) {
            // do not include anything if an invalid line range was found
            return;
          }

          // if no line ranges are configured, include the whole file if it has any content
          if (lineRanges.isEmpty()) {
            if (!includeeText.isEmpty()) {
              pushMarkedInclude(document, reader, target, new StringBuilder(includeeText), 1, includee, attributes);
            }
            return;
          }

          StringBuilder includedText = new StringBuilder();
          int[] firstLine = {0};
          int[] lineNumber = {0};

          includeeText.lines().forEachOrdered(line -> {
            lineNumber[0]++;
            // include a line, if it is contained in any of the configured ranges
            if (lineRanges.stream().anyMatch(lineRange -> lineRange.contains(lineNumber[0]))) {
              if (firstLine[0] == 0) {
                // record the first included line number
                firstLine[0] = lineNumber[0];
              }
              includedText.append(line).append('\n');
            }
          });

          // include matched lines, if there were any in the configured ranges
          if (firstLine[0] != 0) {
            pushMarkedInclude(document, reader, target, includedText, firstLine[0], includee, attributes);
          }
        }

        /**
         * Determine the line ranges to filter for from the attributes.
         *
         * @param attributes The attributes to extract the lines configuration from
         * @return {@code null} if an invalid range was found, otherwise the line ranges to filter the included file for
         */
        private List<Range> determineLineRangesToFilterFor(Map<String, Object> attributes) {
          List<Range> result = new ArrayList<>();
          boolean[] invalidRangeFound = {false};

          splitList(attributes.getOrDefault("lines", "").toString())
            // stop iterating as soon as an invalid range was found
            .takeWhile(__ -> !invalidRangeFound[0])
            .forEach(range -> {
              if (range.contains("..")) {
                // we found a from..to range, extract the bounds like Ruby does it
                String[] bounds = SPLIT_RANGE_PATTERN.split(range, 2);
                Integer from = extractInteger(bounds[0]);
                if ((from == null) || (from == 0)) {
                  invalidRangeFound[0] = true;
                  return;
                }
                Integer to = extractInteger(bounds[1]);
                if ((to == null) || (to < 0)) {
                  to = Integer.MAX_VALUE;
                }
                result.add(new Range(from, to));
              } else {
                // we found a single line, extract it like Ruby does it
                Integer from = extractInteger(range);
                if ((from == null) || (from == 0)) {
                  invalidRangeFound[0] = true;
                  return;
                }
                result.add(new Range(from, from));
              }
            });

          // if invalid range was found return null, so that nothing is included
          return invalidRangeFound[0] ? null : result;
        }

        /**
         * Filter the includee's text for the specified tags and push the result.
         *
         * @param document The currently rendered document
         * @param reader The current reader
         * @param target The target of the include directive, i.e. the included file starting with {@code ../}
         * @param includee The resolved includee
         * @param includeeText The text of the includee
         * @param attributes The attributes used on the include directive
         */
        private void filterForTags(Document document, PreprocessorReader reader, String target,
                                   Path includee, String includeeText, Map<String, Object> attributes) {
          Map<String, Boolean> tags = determineTagsToFilterFor(attributes);

          // if no tags are configured, include the whole file if it has any content
          if (tags.isEmpty()) {
            if (!includeeText.isEmpty()) {
              pushMarkedInclude(document, reader, target, new StringBuilder(includeeText), 1, includee, attributes);
            }
            return;
          }

          // determine the base select value when the tag stack is empty
          // and the wildcard from ** and * tags
          boolean baseSelect;
          Boolean[] wildcard = {null};
          if (tags.containsKey("**")) {
            baseSelect = tags.remove("**");
            if (tags.containsKey("*")) {
              wildcard[0] = tags.remove("*");
            } else if (!baseSelect && !tags.values().iterator().next()) {
              wildcard[0] = true;
            }
          } else if (tags.containsKey("*")) {
            wildcard[0] = tags.remove("*");
            if ("*".equals(tags.keySet().iterator().next())) {
              baseSelect = !wildcard[0];
            } else {
              baseSelect = false;
            }
          } else {
            baseSelect = !tags.containsValue(true);
          }
          boolean[] select = {baseSelect};

          StringBuilder includedText = new StringBuilder();
          record TagStackEntry(String tag, boolean select, int lineNumber) {
          }
          Deque<TagStackEntry> tagStack = new ArrayDeque<>();
          Set<String> selectedTags = new HashSet<>();
          String[] activeTag = {null};
          int[] firstLine = {0};
          int[] lineNumber = {0};

          includeeText.lines().forEachOrdered(line -> {
            lineNumber[0]++;
            Matcher tagLineMatcher = TAG_LINE_PATTERN.matcher(line);
            if (tagLineMatcher.find()) {
              // if we look at a tag start or end line, never include the line
              // but update the tag stack, active tag, and select state
              String thisTag = tagLineMatcher.group("tag");
              if (tagLineMatcher.group("boundary").equals("end")) {
                // if we look at an end tag ...
                if (thisTag.equals(activeTag[0])) {
                  // ... for the currently active tag
                  tagStack.pop();
                  if (tagStack.isEmpty()) {
                    // update the state from the default values
                    activeTag[0] = null;
                    select[0] = baseSelect;
                  } else {
                    // or update the state from the tag stack head
                    TagStackEntry tagStackHead = tagStack.peek();
                    activeTag[0] = tagStackHead.tag();
                    select[0] = tagStackHead.select();
                  }
                } else if (tags.containsKey(thisTag)) {
                  // ... for a different tag on which is filtered too
                  // log a warning about mismatched or unexpected tag end
                  // if the tag in question is not filtered on,
                  // misplaced or interleaved tags are not considered a problem
                  tagStack
                    .stream()
                    .filter(tagStackEntry -> tagStackEntry.tag().equals(thisTag))
                    .findFirst()
                    .ifPresentOrElse(tagStackEntry -> {
                        tagStack.remove(tagStackEntry);
                        log(new LogRecord(WARN, format(
                          "mismatched end tag (expected '%s' but found '%s') at line %d of include file: %s",
                          activeTag[0], thisTag, lineNumber[0], target)));
                      }, () ->
                        log(new LogRecord(WARN, format(
                          "unexpected end tag '%s' at line %d of include file: %s",
                          thisTag, lineNumber[0], target)))
                    );
                }
              } else if (tags.containsKey(thisTag)) {
                // if we look at a start tag that is filtered for
                // update the selected tags, tag stack, active tag, and select state
                select[0] = tags.get(thisTag);
                if (select[0]) {
                  selectedTags.add(thisTag);
                }
                activeTag[0] = thisTag;
                tagStack.push(new TagStackEntry(thisTag, select[0], lineNumber[0]));
              } else if (wildcard[0] != null) {
                // if we look at a start tag that is not filtered for,
                // but we have an explicit or implicit one-star wildcard
                // update the tag stack, active tag, and select state
                select[0] = ((activeTag[0] == null) || select[0]) && wildcard[0];
                activeTag[0] = thisTag;
                tagStack.push(new TagStackEntry(thisTag, select[0], lineNumber[0]));
              }
            } else if (select[0]) {
              // if we do not look at a tag start or end line
              // and current select state is true, include the line
              if (firstLine[0] == 0) {
                // record the first included line number
                firstLine[0] = lineNumber[0];
              }
              includedText.append(line).append('\n');
            }
          });

          // if after all lines are iterated there are still entries on the stack,
          // we found a start tag that was not closed properly, so complain
          if (!tagStack.isEmpty()) {
            tagStack.forEach(tagStackEntry ->
              log(new LogRecord(WARN, format(
                "detected unclosed tag '%s' starting at line %d of include file: %s",
                tagStackEntry.tag(), tagStackEntry.lineNumber(), target))));
          }

          // if there are tags we wanted to include but that were not selected, complain
          tags.entrySet().removeIf(entry -> !entry.getValue() || selectedTags.contains(entry.getKey()));
          if (!tags.isEmpty()) {
            log(new LogRecord(WARN, format(
              "tag%s '%s' not found in include file: %s",
              tags.size() > 1 ? "s" : "", join(", ", tags.keySet()), target)));
          }

          // include matched lines, if there were any in the configured tags specification
          if (firstLine[0] != 0) {
            pushMarkedInclude(document, reader, target, includedText, firstLine[0], includee, attributes);
          }
        }

        /**
         * Determine the tags to filter for from the attributes.
         * To be consistent with the Asciidoctor built-in logic, a {@code tag} attribute wins alone
         * and {@code tags} is ignored even if present additionally.
         * If {@code tag} is not found, {@code tags} is used, split at commas and semicolons
         * without considering surrounding whitespace which stay part of the tag name even if never matching anything.
         * If neither {@code tag} nor {@code tags} is found, the result will be an empty list which results
         * in the whole file being included.
         *
         * @param attributes The attributes to extract the tag configuration from
         * @return The tags to filter the included file for
         */
        private LinkedHashMap<String, Boolean> determineTagsToFilterFor(Map<String, Object> attributes) {
          LinkedHashMap<String, Boolean> result = new LinkedHashMap<>();

          if (attributes.containsKey("tag")) {
            // tag attribute was found, tags will be ignored
            Object tagValue = attributes.get("tag");
            if (tagValue != null) {
              String tag = tagValue.toString();
              if (!tag.isEmpty() && !"!".equals(tag)) {
                // if actually a tag is configured and is not equal to "!", filter for it
                if (tag.charAt(0) == '!') {
                  result.put(tag.substring(1), false);
                } else {
                  result.put(tag, true);
                }
              }
            }
          } else if (attributes.containsKey("tags")) {
            // tag was not found, so use tags value
            Object tagsValue = attributes.get("tags");
            if (tagsValue != null) {
              splitList(tagsValue.toString())
                .forEachOrdered(tag -> {
                  if (!tag.isEmpty() && !"!".equals(tag)) {
                    // if actually a tag is configured and is not equal to "!", filter for it
                    if (tag.charAt(0) == '!') {
                      result.put(tag.substring(1), false);
                    } else {
                      result.put(tag, true);
                    }
                  }
                });
            }
          }

          return result;
        }

        /**
         * Splits a list of tags or line ranges the Asciidoctor way. This means, if a comma is
         * found, split only by comma, otherwise split only be semicolon. Surrounding whitespaces
         * are not considered but stay part of the delimited values.
         *
         * @param list The list to split
         * @return The result of the splitting as {@code Stream<String>}
         */
        private Stream<String> splitList(String list) {
          return (list.indexOf(',') == -1 ? SEMICOLON_DELIMITER_PATTERN : COMMA_DELIMITER_PATTERN)
            .splitAsStream(list);
        }

        /**
         * Extracts an integer from a {@code String} like Ruby does it.
         *
         * @param numberString The string to extract the integer from
         * @return {@code null} if the string does not start with an integer, otherwise the parsed integer
         */
        private Integer extractInteger(String numberString) {
          Matcher numberMatcher = EXTRACT_NUMBER_PATTERN.matcher(numberString);
          if (numberMatcher.matches()) {
            return Integer.valueOf(numberMatcher.group());
          }
          return null;
        }

        /**
         * Read and return the text of the given file, reraising {@code IOException}s as {@code RuntimeException}s
         * and assuming all files are UTF-8 encoded.
         *
         * @param file The file to read the text from
         * @return The text of the given file
         */
        private static String readText(Path file) {
          try {
            return readString(file, UTF_8);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }

        /**
         * Push the include source marker line containing the GitHub link and then the included text to the reader.
         *
         * @param document The currently rendered document
         * @param reader The current reader
         * @param target The target of the include directive, i.e. the included file starting with {@code ../}
         * @param includedText The included text
         * @param firstLine The line within the includee where the first included line is found
         * @param includee The resolved includee
         * @param attributes The attributes to forward
         */
        private void pushMarkedInclude(Document document, PreprocessorReader reader, String target,
                                       StringBuilder includedText, int firstLine, Path includee,
                                       Map<String, Object> attributes) {
          // push to the top of the included text the include source marker looking like
          // "<padding for not disturbing indent handling><includeSourceMarker><link to file and line on GitHub><includeSourceMarker>\n"
          includedText.insert(0, '\n');
          includedText.insert(0, includeSourceMarker);
          includedText.insert(0, firstLine);
          includedText.insert(0, "#L");
          includedText.insert(0, target.substring(3));
          includedText.insert(0, '/');
          includedText.insert(0, document.getAttribute("commit-ish", "master"));
          includedText.insert(0, '/');
          includedText.insert(0, document.getAttribute("github-blob-base"));
          includedText.insert(0, includeSourceMarker);
          includedText.insert(0, INCLUDE_SOURCE_MARKER_PADDING);

          reader.pushInclude(includedText.toString(), includee.getFileName().toString(),
            includee.getParent().toString(), firstLine, attributes);
        }

        /**
         * As we do custom include handling here, we have to reimplement all attribute handling we need.
         * So verify that only currently supported attributes are used.
         *
         * @param reader The current reader
         * @param attributes The attributes used on the include directive
         */
        private void verifyUsedAttributes(PreprocessorReader reader, Map<String, Object> attributes) {
          String unsupportedAttributes = attributes
            .keySet()
            .stream()
            .filter(attribute ->
              "leveloffset".equals(attribute) || "encoding".equals(attribute) || "opts".equals(attribute))
            .collect(joining(", "));

          if (!unsupportedAttributes.isEmpty()) {
            throw new RuntimeException(format("%s: line %d: unsupported attributes in custom include directive, " +
                "if you need them, their logic first has to be replicated in %s: %s",
              reader.getFile(), reader.getLineNumber() - 1, getClass().getSimpleName(), unsupportedAttributes));
          }
        }

        record Range(int from, int to) {
          public boolean contains(int i) {
            return (from <= i) && (i <= to);
          }
        }
      }
    );

    registry.treeprocessor(
      new Treeprocessor() {
        @Override
        public Document process(Document document) {
          processBlocks(document, true);
          processBlocks(document, false);
          return document;
        }

        private void processBlocks(Document document, boolean listings) {
          document
            // find all listing or literal blocks in the document tree
            .findBy(Map.of(
              "traverse_documents", true,
              "context", listings ? LISTING : LITERAL
            ))
            .stream()
            .map(Block.class::cast)
            .forEach(block -> {
              List<String> lines = block.getLines();

              // trim the remainders of the padding that could have been shortened by indent handling
              // and search for lines that then start and end with the include source marker
              List<String> includeSourceMarkerLines = lines
                .stream()
                .filter(line -> line.trim().startsWith(includeSourceMarker) && line.endsWith(includeSourceMarker))
                .toList();

              // if a literal block gets source style, it is transformed to a listing block
              // but this attribute still preserves the information that it originally was a literal block
              boolean cloakedLiteral = block.hasAttribute("cloaked-context") && "literal".equals(block.getAttribute("cloaked-context").toString());

              // if no include source marker lines are found, and we are at a listing block,
              // complain that there is only inline code instead of including tested code;
              // if we are at a literal block, just do nothing and don't complain;
              // be aware that a literal block with `source` style in the sources ends up as listing block here
              // Once https://github.com/asciidoctor/asciidoctor/issues/4556 is implemented we could add a recognition here
              if (includeSourceMarkerLines.isEmpty()) {
                if (listings && !cloakedLiteral && !lines.isEmpty()) {
                  log(new LogRecord(WARN, "listing with only inline code found; " +
                    "if this is not source from a file, consider using a literal block without source style instead; " +
                    "first line: " + lines.get(0)));
                }
                return;
              }

              // get the first include source marker line and
              // remove all such marker lines from the content
              String includeSourceMarkerLine = includeSourceMarkerLines.get(0);
              lines.removeAll(includeSourceMarkerLines);
              block.setLines(lines);

              // construct an AsciiDoc table programmatically that wraps
              // the current block and in AsciiDoc would be looking like
              //
              // [cols="a,0",frame=none,grid=none]
              // |===
              // |
              // <the current block source>
              // |
              // ^icon:github[2x,link=<GitHub file link>,window=_blank]^
              // ^icon:play[2x,link=<gwcBase>?github=<canonicalPath>,window=_blank]^
              // |===

              // construct the table
              Table table = createTable((StructuralNode) block.getParent());
              table.setFrame("none");
              table.setGrid("none");

              // construct the content column
              Column contentColumn = createTableColumn(table, 0);
              contentColumn.setWidth(1);
              // work-around for https://github.com/asciidoctor/asciidoctorj/issues/1265
              contentColumn.setAttribute("colpcwidth", 100, true);
              table.getColumns().add(contentColumn);

              // construct the links column
              Column linksColumn = createTableColumn(table, 1);
              linksColumn.setWidth(0);
              // work-around for https://github.com/asciidoctor/asciidoctorj/issues/1265
              linksColumn.setAttribute("colpcwidth", 0, true);
              table.getColumns().add(linksColumn);

              // add the current block to a cell for the content column
              Document contentCellDocument = createDocument(document);
              contentCellDocument.getBlocks().add(block);
              Cell contentCell = createTableCell(contentColumn, contentCellDocument);

              // construct the cell for the links column
              // first extract the GitHub link from the first include source marker line
              String sourceLink = includeSourceMarkerLine.substring(
                includeSourceMarkerLine.indexOf(includeSourceMarker) + includeSourceMarkerLength,
                includeSourceMarkerLine.length() - includeSourceMarkerLength);
              StringBuilder linksBuilder = new StringBuilder();
              // add the GitHub icon that links to the source file and line on GitHub
              linksBuilder
                .append("^icon:github[2x,link=")
                .append(sourceLink)
                .append(",window=_blank]^");
              // if we are at a literal block, never show the play icon
              // if we are at a listing block, but the `play` attribute was set to non-`true`, never show the play icon
              // if the `gwc-base` document attribute is not set, never show the play icon
              // if the included file's name does not end with `.groovy`, never show the play icon
              // otherwise show the play icon
              // but if the source link contains `/master/` instead of an commit ID,
              // do not link the play icon, as the gwc cannot handle branch names
              if (listings && !cloakedLiteral && parseBoolean(block.getAttribute("play", "true").toString())) {
                Object gwcBase = document.getAttribute("gwc-base");
                if (gwcBase != null) {
                  String canonicalPath = sourceLink.substring(sourceLink.indexOf("github.com/") + "github.com/".length());
                  canonicalPath = canonicalPath.substring(0, canonicalPath.lastIndexOf('#'));
                  if (canonicalPath.endsWith(".groovy")) {
                    linksBuilder.append("\n^icon:play[2x");
                    if (!sourceLink.contains("/master/")) {
                      linksBuilder
                        .append(",link=")
                        .append(gwcBase)
                        .append("?github=")
                        .append(canonicalPath)
                        .append(",window=_blank");
                    }
                    linksBuilder.append("]^");
                  }
                }
              }
              Cell linksCell = createTableCell(linksColumn, linksBuilder.toString());

              // add the cells in a row to the table
              Row row = createTableRow(table);
              row.getCells().add(contentCell);
              row.getCells().add(linksCell);
              table.getBody().add(row);

              // replace the current block in the parent by the table-wrapped version
              List<StructuralNode> blocks = ((StructuralNode) block.getParent()).getBlocks();
              blocks.set(blocks.indexOf(block), table);
            });
        }
      }
    );

    registry.postprocessor(
      new Postprocessor() {
        @Override
        public String process(Document document, String output) {
          // make sure there are no left-overs from the split logic above
          // where the include processor inserts markers
          // that are then transformed in the tree processor
          if (output.contains(includeSourceMarker)) {
            throw new AssertionError("The include source linker left an include source marker somewhere");
          }
          return output;
        }
      }
    );
  }
}
