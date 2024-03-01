package org.spockframework.plugins.asciidoctor;

import org.asciidoctor.ast.*;
import org.asciidoctor.extension.IncludeProcessor;
import org.asciidoctor.extension.PreprocessorReader;
import org.asciidoctor.extension.Treeprocessor;
import org.asciidoctor.log.LogRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.asciidoctor.extension.Contexts.LISTING;
import static org.asciidoctor.extension.Contexts.LITERAL;
import static org.asciidoctor.log.Severity.WARN;

public class IncludedSourceLinker {
  private static final String INCLUDE_SOURCE_MARKER_PADDING = "                                                                                                    ";
  private final String includeSourceMarker = UUID.randomUUID().toString();
  private final int includeSourceMarkerLength = includeSourceMarker.length();

  public IncludeProcessor getIncludeProcessor() {
    return new IncludeProcessor() {
      private static final Pattern TAG_DELIMITER = Pattern.compile("[,;]");
      private static final Pattern TAG_LINE_PATTERN = Pattern.compile("\\b(?<boundary>tag|end)::(?<tag>\\S+?)\\[](?: |$)");

      @Override
      public boolean handles(String target) {
        return target.startsWith("../");
      }

      @Override
      public void process(Document document, PreprocessorReader reader, String target, Map<String, Object> attributes) {
        String unsupportedAttributes = attributes
          .keySet()
          .stream()
          .filter(attribute -> !"tag".equals(attribute) && !"tags".equals(attribute))
          .collect(joining(", "));

        if (!unsupportedAttributes.isEmpty()) {
          throw new RuntimeException(reader.getFile() + ": line " + (reader.getLineNumber() - 1) + ": " + "unsupported attributes in custom include directive: " + unsupportedAttributes);
        }

        try {
          Path includee = Path.of(reader.getFile()).getParent().resolve(target);
          String includeeText = Files.readString(includee, UTF_8);

          List<String> tags = new ArrayList<>();
          Object tagValue = attributes.get("tag");
          if (tagValue != null) {
            tags.add(tagValue.toString());
          } else {
            Object tagsValue = attributes.get("tags");
            if (tagsValue != null) {
              TAG_DELIMITER
                .splitAsStream(tagsValue.toString())
                .forEachOrdered(tags::add);
            }
          }

          if (tags.isEmpty()) {
            reader.pushInclude(includeeText, includee.getFileName().toString(), includee.getParent().toString(), 1, emptyMap());
            return;
          }

          if (tags.stream().filter(tag -> "**".equals(tag) || "!**".equals(tag)).limit(2).count() == 2) {
            throw new IllegalStateException("double wildcard must only be specified once");
          }

          boolean includeAll;
          if (tags.contains("**")) {
            includeAll = true;
            tags.remove("**");
          } else if (tags.contains("!**")) {
            includeAll = false;
            tags.remove("!**");
          } else {
            includeAll = tags.get(0).charAt(0) == '!';
          }

          if (tags.stream().filter(tag -> "*".equals(tag) || "!*".equals(tag)).limit(2).count() == 2) {
            throw new IllegalStateException("single wildcard must only be specified once");
          }

          Map<String, String> tagNameByNegated = tags
            .stream()
            .filter(tag -> tag.charAt(0) == '!')
            .collect(toMap(identity(), tag -> tag.substring(1)));

          StringBuilder includedText = new StringBuilder();
          List<String> currentRegions = new ArrayList<>();
          boolean[] firstLineFound = {false};
          int[] firstLine = {0};
          includeeText.lines().forEachOrdered(line -> {
            Matcher tagLineMatcher = TAG_LINE_PATTERN.matcher(line);
            if (tagLineMatcher.find()) {
              if (!firstLineFound[0]) {
                firstLine[0]++;
              }
              String tag = tagLineMatcher.group("tag");
              if (tagLineMatcher.group("boundary").equals("tag")) {
                currentRegions.add(tag);
              } else {
                currentRegions.remove(tag);
              }
            } else {
              boolean include = includeAll;
              for (String tag : tags) {
                String tagName = tagNameByNegated.getOrDefault(tag, tag);
                if (tag.charAt(0) == '!') {
                  if ("*".equals(tagName)) {
                    include = currentRegions.isEmpty();
                  } else if (currentRegions.contains(tagName)) {
                    include = false;
                  }
                } else {
                  if ("*".equals(tagName)) {
                    include = !currentRegions.isEmpty();
                  } else if (currentRegions.contains(tagName)) {
                    include = true;
                  }
                }
              }

              if (!firstLineFound[0]) {
                firstLine[0]++;
              }
              if (include) {
                firstLineFound[0] = true;
                includedText.append(line).append('\n');
              }
            }
          });

          if (!currentRegions.isEmpty()) {
            log(new LogRecord(
              WARN,
              "Found unclosed tag directive" + ((currentRegions.size() != 1) ? "s" : "") + ": " + join(", ", currentRegions)));
          }

          if (firstLineFound[0]) {
            includedText.insert(0, '\n');
            includedText.insert(0, includeSourceMarker);
            includedText.insert(0, firstLine[0]);
            includedText.insert(0, "#L");
            includedText.insert(0, target.substring(3));
            includedText.insert(0, '/');
            includedText.insert(0, document.getAttribute("commit-ish", "master"));
            includedText.insert(0, '/');
            includedText.insert(0, document.getAttribute("github-blob-base"));
            includedText.insert(0, includeSourceMarker);
            includedText.insert(0, INCLUDE_SOURCE_MARKER_PADDING);

            reader.pushInclude(includedText.toString(), includee.getFileName().toString(), includee.getParent().toString(), firstLine[0], emptyMap());
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  public Treeprocessor getTreeprocessor() {
    return new Treeprocessor() {
      @Override
      public Document process(Document document) {
        document
          .findBy(Map.of(
            "traverse_documents", true,
            "context", LISTING
          ))
          .stream()
          .map(Block.class::cast)
          .forEach(block -> {
            List<String> lines = block.getLines();

            List<String> includeSourceMarkerLines = lines
              .stream()
              .filter(line -> line.trim().startsWith(includeSourceMarker) && line.endsWith(includeSourceMarker))
              .collect(toList());

            if (includeSourceMarkerLines.isEmpty()) {
              if (!lines.isEmpty()) {
                log(new LogRecord(WARN, "listing with only inline code found; " +
                  "if this is not source from a file, consider using a literal block instead; " +
                  "first line: " + lines.get(0)));
              }
              return;
            }

            String includeSourceMarkerLine = includeSourceMarkerLines.get(0);
            lines.removeAll(includeSourceMarkerLines);
            block.setLines(lines);

            Table table = createTable((StructuralNode) block.getParent());
            table.setFrame("none");
            table.setGrid("none");

            Column contentColumn = createTableColumn(table, 0);
            contentColumn.setWidth(1);
            contentColumn.setAttribute("colpcwidth", 100, true);
            table.getColumns().add(contentColumn);

            Column linkColumn = createTableColumn(table, 1);
            linkColumn.setWidth(0);
            linkColumn.setAttribute("colpcwidth", 0, true);
            table.getColumns().add(linkColumn);

            Document contentCellDocument = createDocument(document);
            contentCellDocument.getBlocks().add(block);

            Cell contentCell = createTableCell(contentColumn, contentCellDocument);
            String sourceLink = includeSourceMarkerLine.substring(
              includeSourceMarkerLine.indexOf(includeSourceMarker) + includeSourceMarkerLength,
              includeSourceMarkerLine.length() - includeSourceMarkerLength);
            Cell linkCell = createTableCell(linkColumn, "^icon:github[2x,link=" + sourceLink + ",window=_blank]^");

            Row row = createTableRow(table);
            row.getCells().add(contentCell);
            row.getCells().add(linkCell);
            table.getBody().add(row);

            List<StructuralNode> blocks = ((StructuralNode) block.getParent()).getBlocks();
            blocks.set(blocks.indexOf(block), table);
          });

        document
          .findBy(Map.of(
            "traverse_documents", true,
            "context", LITERAL
          ))
          .stream()
          .map(Block.class::cast)
          .forEach(block -> block.setLines(
            block
              .getLines()
              .stream()
              .filter(line -> !(line.trim().startsWith(includeSourceMarker) && line.endsWith(includeSourceMarker)))
              .collect(toList())
          ));

        return document;
      }
    };
  }
}
