package org.spockframework.plugins.asciidoctor;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.jruby.converter.spi.ConverterRegistry;

public class MarkdownConverterRegistry implements ConverterRegistry {
  @Override
  public void register(Asciidoctor asciidoctor) {
    asciidoctor.javaConverterRegistry().register(MarkdownConverter.class);
  }
}
