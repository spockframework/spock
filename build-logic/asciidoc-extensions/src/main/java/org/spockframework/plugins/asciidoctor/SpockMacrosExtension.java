package org.spockframework.plugins.asciidoctor;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

public class SpockMacrosExtension implements ExtensionRegistry {

  @Override
  public void register(Asciidoctor asciidoctor) {
    var registry = asciidoctor.javaExtensionRegistry();
    registry.inlineMacro(SpockIssueInlineMacroProcessor.class);
    registry.inlineMacro(SpockPullInlineMacroProcessor.class);
  }
}
