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
      case "open"            -> convertOpen((StructuralNode) node);
      case "sidebar"         -> convertSidebar((StructuralNode) node);
      case "example"         -> convertExample((StructuralNode) node);
      case "thematic_break"  -> "---\n\n";
      case "inline_quoted"   -> convertInlineQuoted((PhraseNode) node);
      case "inline_anchor"   -> convertInlineAnchor((PhraseNode) node);
      case "inline_image"    -> convertInlineImage((PhraseNode) node);
      case "inline_footnote" -> convertInlineFootnote((PhraseNode) node);
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
    Object content = node.getContent();
    if (content != null) {
      sb.append(content);
    }
    return sb.toString();
  }

  private String convertSection(Section node) {
    var sb = new StringBuilder();
    // Section level 0 = top-level section = ##, level 1 = ###, etc.
    int headingLevel = node.getLevel() + 1;
    sb.append("#".repeat(Math.min(headingLevel, 6)))
      .append(" ")
      .append(decodeEntities(node.getTitle()))
      .append("\n\n");
    Object content = node.getContent();
    if (content != null) {
      sb.append(content);
    }
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
    String type = node.getType();
    return switch (type) {
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
    String target = node.getTarget();
    String alt = node.getAttribute("alt", "").toString();
    return "![" + alt + "](" + target + ")";
  }

  private String convertInlineFootnote(PhraseNode node) {
    String text = decodeEntities(node.getText());
    if (text == null || text.isEmpty()) return "";
    return " (Note: " + text + ")";
  }

  // --- Code block converters ---

  private String convertListing(Block node) {
    var sb = new StringBuilder();
    String blockTitle = node.getTitle();
    if (blockTitle != null) {
      sb.append("**").append(blockTitle).append("**\n\n");
    }
    sb.append("```");
    String style = node.getStyle();
    if ("source".equals(style)) {
      Object lang = node.getAttribute("language");
      if (lang != null) {
        sb.append(lang);
      }
    } else if (style != null && !"listing".equals(style)) {
      // Diagram block (plantuml, ditaa, etc.) — use style as language
      sb.append(style);
    }
    sb.append("\n");
    String source = node.getSource();
    if (source != null) {
      sb.append(source);
      if (!source.endsWith("\n")) {
        sb.append("\n");
      }
    }
    sb.append("```\n\n");
    return sb.toString();
  }

  private String convertLiteral(Block node) {
    var sb = new StringBuilder();
    String blockTitle = node.getTitle();
    if (blockTitle != null) {
      sb.append("**").append(blockTitle).append("**\n\n");
    }
    sb.append("```");
    String style = node.getStyle();
    if (style != null && !"literal".equals(style)) {
      // Diagram block (plantuml, ditaa, etc.) — use style as language
      sb.append(style);
    }
    sb.append("\n");
    String source = node.getSource();
    if (source != null) {
      sb.append(source);
      if (!source.endsWith("\n")) {
        sb.append("\n");
      }
    }
    sb.append("```\n\n");
    return sb.toString();
  }

  // --- List converters ---

  private String convertUnorderedList(List node) {
    var sb = new StringBuilder();
    String blockTitle = node.getTitle();
    if (blockTitle != null) {
      sb.append("**").append(blockTitle).append("**\n\n");
    }
    appendUnorderedItems(sb, node, 0);
    sb.append("\n");
    return sb.toString();
  }

  private void appendUnorderedItems(StringBuilder sb, List node, int depth) {
    String indent = "  ".repeat(depth);
    for (StructuralNode item : node.getItems()) {
      ListItem listItem = (ListItem) item;
      sb.append(indent).append("- ");
      if (listItem.hasText()) {
        sb.append(decodeEntities(listItem.getText()));
      }
      sb.append("\n");
      for (StructuralNode block : listItem.getBlocks()) {
        if (block instanceof List nestedList) {
          appendUnorderedItems(sb, nestedList, depth + 1);
        } else {
          String converted = block.convert();
          if (converted != null && !converted.isEmpty()) {
            for (String line : converted.split("\n", -1)) {
              sb.append(indent).append("  ").append(line).append("\n");
            }
          }
        }
      }
    }
  }

  private String convertOrderedList(List node) {
    var sb = new StringBuilder();
    String blockTitle = node.getTitle();
    if (blockTitle != null) {
      sb.append("**").append(blockTitle).append("**\n\n");
    }
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
      if (listItem.hasText()) {
        sb.append(decodeEntities(listItem.getText()));
      }
      sb.append("\n");
      for (StructuralNode block : listItem.getBlocks()) {
        if (block instanceof List nestedList) {
          if ("olist".equals(nestedList.getContext())) {
            appendOrderedItems(sb, nestedList, depth + 1);
          } else {
            appendUnorderedItems(sb, nestedList, depth + 1);
          }
        } else {
          String converted = block.convert();
          if (converted != null && !converted.isEmpty()) {
            String padding = indent + "   ";
            for (String line : converted.split("\n", -1)) {
              sb.append(padding).append(line).append("\n");
            }
          }
        }
      }
      number++;
    }
  }

  private String convertDescriptionList(DescriptionList node) {
    var sb = new StringBuilder();
    String blockTitle = node.getTitle();
    if (blockTitle != null) {
      sb.append("**").append(blockTitle).append("**\n\n");
    }
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
    String blockTitle = node.getTitle();
    if (blockTitle != null) {
      sb.append("**").append(blockTitle).append("**\n\n");
    }
    java.util.List<Row> headerRows = node.getHeader();
    java.util.List<Row> bodyRows = node.getBody();
    int colCount = node.getColumns().size();

    if (!headerRows.isEmpty()) {
      Row headerRow = headerRows.get(0);
      sb.append("|");
      for (Cell cell : headerRow.getCells()) {
        sb.append(" ").append(cellText(cell)).append(" |");
      }
      sb.append("\n");
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
      sb.append("|");
      for (Cell cell : row.getCells()) {
        sb.append(" ").append(cellText(cell)).append(" |");
      }
      sb.append("\n");
    }
    sb.append("\n");
    return sb.toString();
  }

  private String cellText(Cell cell) {
    String style = cell.getStyle();
    if ("asciidoc".equals(style)) {
      Document innerDoc = cell.getInnerDocument();
      if (innerDoc != null) {
        String content = innerDoc.getContent() != null ? innerDoc.getContent().toString() : "";
        return decodeEntities(content.strip().replaceAll("\n+", " "));
      }
    }
    String text = cell.getText();
    if (text == null) return "";
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
    Object content = node.getContent();
    if (content != null) {
      for (String line : decodeEntities(content.toString()).split("\n", -1)) {
        sb.append("> ").append(line).append("\n");
      }
    }
    sb.append("\n");
    return sb.toString();
  }

  private String convertImage(StructuralNode node) {
    String target = (String) node.getAttribute("target");
    String alt = node.getAttribute("alt", "").toString();
    var sb = new StringBuilder();
    String blockTitle = node.getTitle();
    if (blockTitle != null) {
      sb.append("**").append(blockTitle).append("**\n\n");
    }
    sb.append("![").append(alt).append("](").append(target).append(")\n\n");
    return sb.toString();
  }

  // --- Container block converters ---

  private String convertOpen(StructuralNode node) {
    var sb = new StringBuilder();
    String blockTitle = node.getTitle();
    if (blockTitle != null) {
      sb.append("**").append(blockTitle).append("**\n\n");
    }
    Object content = node.getContent();
    if (content != null) {
      sb.append(content);
    }
    return sb.toString();
  }

  private String convertSidebar(StructuralNode node) {
    var sb = new StringBuilder();
    String blockTitle = node.getTitle();
    if (blockTitle != null) {
      sb.append("**").append(blockTitle).append("**\n\n");
    }
    Object content = node.getContent();
    if (content != null) {
      sb.append(content);
    }
    return sb.toString();
  }

  private String convertExample(StructuralNode node) {
    var sb = new StringBuilder();
    String blockTitle = node.getTitle();
    if (blockTitle != null) {
      sb.append("**").append(blockTitle).append("**\n\n");
    }
    Object content = node.getContent();
    if (content != null) {
      sb.append(content);
    }
    return sb.toString();
  }

  // --- Utility methods ---

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
