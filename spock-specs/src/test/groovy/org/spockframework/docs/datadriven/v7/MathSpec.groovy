package org.spockframework.docs.datadriven.v7

import spock.lang.Specification

// tag::example[]
class MathSpec extends Specification {
  def "maximum of two numbers"(
    // end::example[]
    int a, int b, int d, int e
    // tag::example[]
  ) {
    expect:
    Math.max(a, b) == c
    Math.max(d, e) == f

    where:
    a | b || c
    1 | 3 || 3
    7 | 4 || 7
    0 | 0 || 0

    d ; e ;; f
    1 ; 3 ;; 3
    7 ; 4 ;; 7
    0 ; 0 ;; 0
  }
}
// end::example[]
