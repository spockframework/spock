package org.spockframework.plugins.asciidoctor;

import org.asciidoctor.ast.*;
import org.asciidoctor.converter.ConverterFor;
import org.asciidoctor.converter.StringConverter;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;

import java.util.Map;

@ConverterFor(value = "markdown", suffix = ".md")
public class MarkdownConverter extends StringConverter {

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
      .append(node.getTitle())
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
    return content.toString() + "\n\n";
  }

  private String convertPreamble(StructuralNode node) {
    Object content = node.getContent();
    return content != null ? content.toString() : "";
  }

  // Stubs — implemented in subsequent tasks

  private String convertListing(Block node) { return ""; }
  private String convertLiteral(Block node) { return ""; }
  private String convertUnorderedList(List node) { return ""; }
  private String convertOrderedList(List node) { return ""; }
  private String convertDescriptionList(DescriptionList node) { return ""; }
  private String convertTable(Table node) { return ""; }
  private String convertAdmonition(StructuralNode node) { return ""; }
  private String convertImage(StructuralNode node) { return ""; }
  private String convertOpen(StructuralNode node) { return ""; }
  private String convertSidebar(StructuralNode node) { return ""; }
  private String convertExample(StructuralNode node) { return ""; }
  private String convertInlineQuoted(PhraseNode node) { return ""; }
  private String convertInlineAnchor(PhraseNode node) { return ""; }
  private String convertInlineImage(PhraseNode node) { return ""; }
  private String convertInlineFootnote(PhraseNode node) { return ""; }
}
