package org.spockframework.smoke.condition

import spock.lang.Issue
import spock.lang.Specification

class ConditionG4Spec extends Specification {

  @Issue("https://github.com/spockframework/spock/issues/1956")
  def "test range"() {
    expect:
    (0..5) == [0, 1, 2, 3, 4, 5]
    (0<..5) == [1, 2, 3, 4, 5]
    (0..<5) == [0, 1, 2, 3, 4]
    (0<..<5) == [1, 2, 3, 4]
  }
}
