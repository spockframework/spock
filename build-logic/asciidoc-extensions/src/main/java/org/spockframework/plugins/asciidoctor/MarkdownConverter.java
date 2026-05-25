package org.spockframework.plugins.asciidoctor;

import org.asciidoctor.ast.*;
import org.asciidoctor.converter.ConverterFor;
import org.asciidoctor.converter.StringConverter;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ConverterFor(value = "markdown", suffix = ".md")
public class MarkdownConverter extends StringConverter {

  private static final Pattern HTML_ENTITY_PATTERN = Pattern.compile("&#(\\d+);");
  private static final Pattern NAMED_ENTITY_PATTERN = Pattern.compile("&(lt|gt|amp|quot|apos);");
  private static final Pattern ZERO_WIDTH_SPACE_PATTERN = Pattern.compile("\u200B");

  public MarkdownConverter(String backend, Map<String, Object> opts) {
    super(backend, opts);
  }

  @Override
  public String convert(ContentNode node, String transform, Map<Object, Object> opts) {
    String name = transform != null ? transform : node.getNodeName();
    return switch (name) {
      case "document"        -> convertDocument((Document) node);
      case "section"         -> convertSection((Section) node);
      case "paragraph"       -> convertParagraph((StructuralNode) node);
      case "preamble"        -> convertPreamble((StructuralNode) node);
      case "listing"         -> convertListing((Block) node);
      case "literal"         -> convertLiteral((Block) node);
      case "ulist"           -> convertUnorderedList((List) node);
      case "olist"           -> convertOrderedList((List) node);
      case "dlist"           -> convertDescriptionList((DescriptionList) node);
      case "table"           -> convertTable((Table) node);
      case "admonition"      -> convertAdmonition((StructuralNode) node);
      case "image"           -> convertImage((StructuralNode) node);
      case "open", "sidebar", "example"
                             -> convertContentBlock((StructuralNode) node);
      case "thematic_break"  -> "---\n\n";
      case "inline_quoted"   -> convertInlineQuoted((PhraseNode) node);
      case "inline_anchor"   -> convertInlineAnchor((PhraseNode) node);
      case "inline_image"    -> convertInlineImage((PhraseNode) node);
      case "inline_footnote" -> convertInlineFootnote((PhraseNode) node);
      case "inline_break"    -> convertInlineBreak((PhraseNode) node);
      case "colist"          -> convertOrderedList((List) node);
      case "quote"           -> convertQuote((StructuralNode) node);
      default -> {
        log(new LogRecord(Severity.WARN, "Unsupported node: " + name));
        yield "";
      }
    };
  }

  private String convertDocument(Document node) {
    var sb = new StringBuilder();
    String title = node.getDoctitle();
    if (title != null) {
      sb.append("# ").append(title).append("\n\n");
    }
    appendContent(sb, node);
    return sb.toString();
  }

  private String convertSection(Section node) {
    var sb = new StringBuilder();
    int headingLevel = node.getLevel() + 1;
    sb.append("#".repeat(Math.min(headingLevel, 6)))
      .append(" ")
      .append(decodeEntities(node.getTitle()))
      .append("\n\n");
    appendContent(sb, node);
    return sb.toString();
  }

  private String convertParagraph(StructuralNode node) {
    Object content = node.getContent();
    if (content == null) return "";
    return decodeEntities(content.toString()) + "\n\n";
  }

  private String convertPreamble(StructuralNode node) {
    Object content = node.getContent();
    return content != null ? content.toString() : "";
  }

  // --- Inline converters ---

  private String convertInlineQuoted(PhraseNode node) {
    String text = decodeEntities(node.getText());
    if (text == null) return "";
    return switch (node.getType()) {
      case "strong"      -> "**" + text + "**";
      case "emphasis"    -> "*" + text + "*";
      case "monospaced"  -> "`" + text + "`";
      case "double"      -> "\u201c" + text + "\u201d";
      case "single"      -> "\u2018" + text + "\u2019";
      case "superscript" -> "<sup>" + text + "</sup>";
      case "subscript"   -> "<sub>" + text + "</sub>";
      case "mark"        -> "<mark>" + text + "</mark>";
      default            -> text;
    };
  }

  private String convertInlineAnchor(PhraseNode node) {
    return switch (node.getType()) {
      case "link" -> {
        String text = decodeEntities(node.getText());
        String target = node.getTarget();
        if (text == null || text.isEmpty()) {
          yield "[" + target + "](" + target + ")";
        }
        // When text equals target (auto-linked URL), output just the URL
        // to avoid double-wrapping when the framework creates a nested link node
        if (text.equals(target)) {
          yield target;
        }
        yield "[" + text + "](" + target + ")";
      }
      case "xref" -> {
        String text = decodeEntities(node.getText());
        String refid = node.getAttribute("refid", "").toString();
        String path = node.getAttribute("path", "").toString();
        String fragment = node.getAttribute("fragment", "").toString();
        var target = new StringBuilder();
        if (!path.isEmpty()) {
          target.append(path.replace(".adoc", ".md"));
        }
        if (!fragment.isEmpty()) {
          target.append("#").append(fragment);
        } else if (!refid.isEmpty() && path.isEmpty()) {
          target.append("#").append(refid);
        }
        if (text == null || text.isEmpty()) {
          text = refid.isEmpty() ? target.toString() : refid;
        }
        yield "[" + text + "](" + target + ")";
      }
      case "bibref" -> {
        String text = node.getText();
        yield text != null ? "[" + text + "]" : "";
      }
      case "ref" -> "";
      default -> {
        String text = node.getText();
        yield text != null ? text : "";
      }
    };
  }

  private String convertInlineImage(PhraseNode node) {
    return "![" + node.getAttribute("alt", "") + "](" + node.getTarget() + ")";
  }

  private String convertInlineBreak(PhraseNode node) {
    String text = decodeEntities(node.getText());
    if (text == null || text.isEmpty()) return "\n";
    return text + "\n";
  }

  private String convertInlineFootnote(PhraseNode node) {
    String text = decodeEntities(node.getText());
    if (text == null || text.isEmpty()) return "";
    return " (Note: " + text + ")";
  }

  // --- Code block converters ---

  private String convertListing(Block node) {
    String style = node.getStyle();
    String lang = null;
    if ("source".equals(style)) {
      Object langAttr = node.getAttribute("language");
      if (langAttr != null) lang = langAttr.toString();
    } else if (style != null && !"listing".equals(style)) {
      lang = style; // diagram block (plantuml, ditaa, etc.)
    }
    return fencedCodeBlock(node, lang);
  }

  private String convertLiteral(Block node) {
    String style = node.getStyle();
    String lang = (style != null && !"literal".equals(style)) ? style : null;
    return fencedCodeBlock(node, lang);
  }

  // --- List converters ---

  private String convertUnorderedList(List node) {
    var sb = new StringBuilder();
    appendBlockTitle(sb, node);
    appendUnorderedItems(sb, node, 0);
    sb.append("\n");
    return sb.toString();
  }

  private void appendUnorderedItems(StringBuilder sb, List node, int depth) {
    String indent = "  ".repeat(depth);
    for (StructuralNode item : node.getItems()) {
      ListItem listItem = (ListItem) item;
      sb.append(indent).append("- ");
      appendListItemText(sb, listItem);
      sb.append("\n");
      appendListItemBlocks(sb, listItem, indent + "  ", depth);
    }
  }

  private String convertOrderedList(List node) {
    var sb = new StringBuilder();
    appendBlockTitle(sb, node);
    appendOrderedItems(sb, node, 0);
    sb.append("\n");
    return sb.toString();
  }

  private void appendOrderedItems(StringBuilder sb, List node, int depth) {
    String indent = "  ".repeat(depth);
    int number = 1;
    for (StructuralNode item : node.getItems()) {
      ListItem listItem = (ListItem) item;
      sb.append(indent).append(number).append(". ");
      appendListItemText(sb, listItem);
      sb.append("\n");
      appendListItemBlocks(sb, listItem, indent + "   ", depth);
      number++;
    }
  }

  private String convertDescriptionList(DescriptionList node) {
    var sb = new StringBuilder();
    appendBlockTitle(sb, node);
    for (DescriptionListEntry entry : node.getItems()) {
      for (ListItem term : entry.getTerms()) {
        sb.append("**").append(decodeEntities(term.getText())).append("**\n");
      }
      ListItem description = entry.getDescription();
      if (description != null) {
        if (description.hasText()) {
          sb.append(": ").append(decodeEntities(description.getText())).append("\n");
        }
        for (StructuralNode block : description.getBlocks()) {
          String converted = block.convert();
          if (converted != null && !converted.isEmpty()) {
            sb.append("\n").append(converted);
          }
        }
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  // --- Table, admonition, image converters ---

  private String convertTable(Table node) {
    var sb = new StringBuilder();
    appendBlockTitle(sb, node);
    java.util.List<Row> headerRows = node.getHeader();
    java.util.List<Row> bodyRows = node.getBody();
    int colCount = node.getColumns().size();

    if (!headerRows.isEmpty()) {
      appendTableRow(sb, headerRows.get(0));
    } else if (!bodyRows.isEmpty()) {
      sb.append("|");
      for (int i = 0; i < colCount; i++) {
        sb.append("  |");
      }
      sb.append("\n");
    }

    sb.append("|");
    for (int i = 0; i < colCount; i++) {
      sb.append(" --- |");
    }
    sb.append("\n");

    for (Row row : bodyRows) {
      appendTableRow(sb, row);
    }
    sb.append("\n");
    return sb.toString();
  }

  private String cellText(Cell cell) {
    String text;
    if ("asciidoc".equals(cell.getStyle())) {
      Document innerDoc = cell.getInnerDocument();
      text = (innerDoc != null && innerDoc.getContent() != null) ? innerDoc.getContent().toString() : "";
    } else {
      text = cell.getText();
      if (text == null) return "";
    }
    return decodeEntities(text.strip().replaceAll("\n+", " "));
  }

  private String convertAdmonition(StructuralNode node) {
    String style = node.getStyle();
    String alertType = switch (style != null ? style.toUpperCase() : "") {
      case "NOTE"      -> "NOTE";
      case "TIP"       -> "TIP";
      case "WARNING"   -> "WARNING";
      case "IMPORTANT" -> "IMPORTANT";
      case "CAUTION"   -> "CAUTION";
      default          -> "NOTE";
    };
    var sb = new StringBuilder();
    sb.append("> [!").append(alertType).append("]\n");
    appendPrefixedContent(sb, node, "> ");
    sb.append("\n");
    return sb.toString();
  }

  private String convertImage(StructuralNode node) {
    var sb = new StringBuilder();
    appendBlockTitle(sb, node);
    sb.append("![").append(node.getAttribute("alt", "")).append("](")
      .append(node.getAttribute("target")).append(")\n\n");
    return sb.toString();
  }

  // --- Container block converters ---

  private String convertContentBlock(StructuralNode node) {
    var sb = new StringBuilder();
    appendBlockTitle(sb, node);
    appendContent(sb, node);
    return sb.toString();
  }

  private String convertQuote(StructuralNode node) {
    var sb = new StringBuilder();
    appendBlockTitle(sb, node);
    appendPrefixedContent(sb, node, "> ");
    Object attribution = node.getAttribute("attribution");
    if (attribution != null) {
      sb.append(">\n> — ").append(attribution).append("\n");
    }
    sb.append("\n");
    return sb.toString();
  }

  // --- Helpers ---

  private static void appendBlockTitle(StringBuilder sb, StructuralNode node) {
    String title = node.getTitle();
    if (title != null) {
      sb.append("**").append(title).append("**\n\n");
    }
  }

  private static void appendContent(StringBuilder sb, StructuralNode node) {
    Object content = node.getContent();
    if (content != null) {
      sb.append(content);
    }
  }

  private void appendPrefixedContent(StringBuilder sb, StructuralNode node, String prefix) {
    Object content = node.getContent();
    if (content != null) {
      for (String line : decodeEntities(content.toString()).split("\n", -1)) {
        sb.append(prefix).append(line).append("\n");
      }
    }
  }

  private String fencedCodeBlock(Block node, String lang) {
    var sb = new StringBuilder();
    appendBlockTitle(sb, node);
    sb.append("```");
    if (lang != null) sb.append(lang);
    sb.append("\n");
    String source = node.getSource();
    if (source != null) {
      sb.append(source);
      if (!source.endsWith("\n")) sb.append("\n");
    }
    sb.append("```\n\n");
    return sb.toString();
  }

  private void appendListItemText(StringBuilder sb, ListItem item) {
    if (item.hasText()) {
      sb.append(decodeEntities(item.getText()).replace("\n\n", "\n"));
    }
  }

  private void appendListItemBlocks(StringBuilder sb, ListItem item, String padding, int depth) {
    for (StructuralNode block : item.getBlocks()) {
      if (block instanceof List nestedList) {
        if ("olist".equals(nestedList.getContext())) {
          appendOrderedItems(sb, nestedList, depth + 1);
        } else {
          appendUnorderedItems(sb, nestedList, depth + 1);
        }
      } else {
        String converted = block.convert();
        if (converted != null && !converted.isEmpty()) {
          for (String line : converted.split("\n", -1)) {
            sb.append(padding).append(line).append("\n");
          }
        }
      }
    }
  }

  private void appendTableRow(StringBuilder sb, Row row) {
    sb.append("|");
    for (Cell cell : row.getCells()) {
      sb.append(" ").append(cellText(cell)).append(" |");
    }
    sb.append("\n");
  }

  private static String decodeEntities(String text) {
    if (text == null) return null;
    // Decode numeric HTML entities (&#8217; etc.)
    text = HTML_ENTITY_PATTERN.matcher(text).replaceAll(mr -> {
      int codePoint = Integer.parseInt(mr.group(1));
      return Matcher.quoteReplacement(new String(Character.toChars(codePoint)));
    });
    // Decode named HTML entities
    text = NAMED_ENTITY_PATTERN.matcher(text).replaceAll(mr -> switch (mr.group(1)) {
      case "lt"   -> "<";
      case "gt"   -> ">";
      case "amp"  -> "&";
      case "quot" -> Matcher.quoteReplacement("\"");
      case "apos" -> "'";
      default     -> mr.group(0);
    });
    // Remove zero-width spaces
    text = ZERO_WIDTH_SPACE_PATTERN.matcher(text).replaceAll("");
    return text;
  }

}
