package org.spockframework.docs.datadriven.v2

import spock.lang.Specification

// tag::example[]
// tag::example-a[]
class MathSpec extends Specification {
  def "maximum of two numbers"(int a, int b, int c) {
    expect:
    Math.max(a, b) == c
// end::example-a[]

    where:
    a | b | c
    1 | 3 | 3
    7 | 4 | 7
    0 | 0 | 0
// tag::example-b[]
  }
}
// end::example-b[]
// end::example[]
