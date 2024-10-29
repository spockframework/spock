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

  @Issue("https://github.com/spockframework/spock/issues/1845")
  def "explicit assert in switch expression"() {
    expect:
    def b = 3
    !!switch (b) {
      case 3 -> assert 1 == 1
      default -> assert 1 == 1
    }
  }
}
