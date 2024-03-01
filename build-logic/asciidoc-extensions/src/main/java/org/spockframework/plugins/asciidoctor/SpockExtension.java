package org.spockframework.plugins.asciidoctor;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

public class SpockExtension implements ExtensionRegistry {

  @Override
  public void register(Asciidoctor asciidoctor) {
    var registry = asciidoctor.javaExtensionRegistry();
    registry.inlineMacro(SpockIssueInlineMacroProcessor.class);
    registry.inlineMacro(SpockPullInlineMacroProcessor.class);

    IncludedSourceLinker includedSourceLinker = new IncludedSourceLinker();
    registry.includeProcessor(includedSourceLinker.getIncludeProcessor());
    registry.treeprocessor(includedSourceLinker.getTreeprocessor());
  }
}
