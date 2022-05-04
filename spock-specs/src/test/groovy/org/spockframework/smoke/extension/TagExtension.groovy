package org.spockframework.smoke.extension

import spock.lang.Specification
import spock.lang.Tag

@Tag("class-level")
class TagExtension extends Specification {
  def "class level tags"() {
    expect:
    specificationContext.currentFeature.testTags.value == ["class-level"]
  }

  @Tag("method-level")
  def "method level tags"() {
    expect:
    specificationContext.currentFeature.testTags.value =~ ["class-level", "method-level"]
  }
}

class InheritedTagExtension extends TagExtension {
  def "inherited tags"() {
    expect:
    specificationContext.currentFeature.testTags.value =~ ["class-level"]
  }
}
