package org.spockframework.docs.extension

import spock.lang.Specification
import spock.lang.Tag

// tag::tag-extension[]
@Tag("docs")
class TagDocSpec extends Specification {
  def "has one tag"() {
    expect: true
  }

  @Tag("other")
  def "has two tags"() {
    expect: true
  }
}
// end::tag-extension[]
