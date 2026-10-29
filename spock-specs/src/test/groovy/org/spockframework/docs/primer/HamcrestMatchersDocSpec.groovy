package org.spockframework.docs.primer

import spock.lang.Specification

// tag::hamcrest-imports[]
import static spock.util.matcher.HamcrestMatchers.closeTo
import static spock.util.matcher.HamcrestSupport.expect
import static spock.util.matcher.HamcrestSupport.that
// end::hamcrest-imports[]

class HamcrestMatchersDocSpec extends Specification {

  // tag::hamcrest-expect[]
  def "use a matcher in an expect block"() {
    expect:
    42 closeTo(42, 0.001)
  }
  // end::hamcrest-expect[]

  // tag::hamcrest-then[]
  def "use a matcher in a then block"() {
    when:
    def answer = 42

    then:
    expect answer, closeTo(42, 0.001)
  }
  // end::hamcrest-then[]

  // tag::hamcrest-assert[]
  def setup() {
    def answer = 42
    assert that(answer, closeTo(42, 0.001))
  }
  // end::hamcrest-assert[]

  def "setup matcher assertion passes"() {
    expect:
    true
  }
}
