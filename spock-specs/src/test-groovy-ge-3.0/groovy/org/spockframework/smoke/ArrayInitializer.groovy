package org.spockframework.smoke

import spock.lang.Specification

class ArrayInitializer extends Specification {

  def "array initializer in condition does not throw NullPointerException"() {
    expect:
    new Object[] { _ }
  }
}
