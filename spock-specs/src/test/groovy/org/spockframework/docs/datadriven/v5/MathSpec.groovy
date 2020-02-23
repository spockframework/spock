package org.spockframework.docs.datadriven.v5

import spock.lang.Specification

class MathSpec extends Specification {
// tag::example[]
  def "maximum of #a and #b is #c"() {
// end::example[]
    expect:
    Math.max(a, b) == c

    where:
    a | b || c
    1 | 3 || 3
    7 | 4 || 7
    0 | 0 || 0
  }
}
