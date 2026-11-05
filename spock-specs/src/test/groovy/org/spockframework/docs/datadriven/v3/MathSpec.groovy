package org.spockframework.docs.datadriven.v3

import spock.lang.Specification

// tag::example[]
class MathSpec extends Specification {
  def "maximum of two numbers"(
    // end::example[]
    int a, int b
    // tag::example[]
  ) {
    expect:
    Math.max(a, b) == c

    where:
    a | b || c
    1 | 3 || 3
    7 | 4 || 7
    0 | 0 || 0
  }
}
// end::example[]
