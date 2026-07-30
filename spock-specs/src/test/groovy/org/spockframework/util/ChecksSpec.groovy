package org.spockframework.util

import spock.lang.Specification

class ChecksSpec extends Specification {
  def "notNull(object, String) returns the object when not null"() {
    given:
    def value = "x"

    expect:
    Checks.notNull(value, "must not be null").is(value)
  }

  def "notNull(object, String) throws IllegalArgumentException with the message when null"() {
    when:
    Checks.notNull(null, "must not be null")

    then:
    IllegalArgumentException e = thrown()
    e.message == "must not be null"
  }
}
