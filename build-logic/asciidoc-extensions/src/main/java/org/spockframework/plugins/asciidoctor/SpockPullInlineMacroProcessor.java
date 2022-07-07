package org.spockframework.plugins.asciidoctor;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Name;

import java.util.HashMap;
import java.util.Map;

@Name("spockPull")
public class SpockPullInlineMacroProcessor extends InlineMacroProcessor {

  @Override
  public Object process(
    ContentNode parent, String target, Map<String, Object> attributes) {

    String href = "https://github.com/spockframework/spock/pull/" + target;

    Map<String, Object> options = new HashMap<>();
    options.put("type", ":link");
    options.put("target", href);
    return createPhraseNode(parent, "anchor", "#" + target, attributes, options);
  }

}
