package org.spockframework.runtime.model

import spock.lang.Specification

class TestTagTest extends Specification {

  def "allows valid tags" () {
    expect:
    TestTag.create(tag)

    where:
    tag << ['tag', 'tag_with_underscore', 'tag-with-dash', 'ʘ‿ʘ', '♥╣[-_-]╠♥️', '💩', /"'..?/]
  }

  def "forbids invalid characters in tags"(){
    when:
    TestTag.create(tag)

    then:
    IllegalArgumentException e = thrown()
    e.message.contains(readableName)

    where:
    tag << [' ', '\n', '\t', '\r', '\u0000', '(', ')', '&', '|', '!']
    readableName = Character.getName(tag.codePointAt(0))
  }

  def "forbids empty or null tags"() {
    when:
    TestTag.create(tag)

    then:
    thrown(IllegalArgumentException)

    where:
    tag << ['', null]
  }
}
