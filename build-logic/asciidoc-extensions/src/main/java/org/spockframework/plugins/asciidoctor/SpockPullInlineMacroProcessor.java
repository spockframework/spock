package org.spockframework.plugins.asciidoctor;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.PhraseNode;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Name;

import java.util.HashMap;
import java.util.Map;

@Name("spockPull")
public class SpockPullInlineMacroProcessor extends InlineMacroProcessor {

  @Override
  public PhraseNode process(StructuralNode parent, String target, Map<String, Object> attributes) {

    String href = parent.getDocument().getAttribute("github-base") + "/pull/" + target;

    Map<String, Object> options = new HashMap<>();
    options.put("type", ":link");
    options.put("target", href);
    return createPhraseNode(parent, "anchor", "#" + target, attributes, options);
  }

}
