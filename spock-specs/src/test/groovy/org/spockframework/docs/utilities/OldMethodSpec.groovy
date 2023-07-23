package org.spockframework.docs.utilities

import spock.lang.Specification

class OldMethodSpec extends Specification {

  // tag::old-usage-spec[]
  def "Usage of the old() method"() {
    given:
    def x = 0

    when:
    x++

    then:
    x == old(x) + 1
  }
  // end::old-usage-spec[]
}
