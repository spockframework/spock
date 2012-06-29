package org.spockframework.smoke.mock

import org.spockframework.mock.IMockInvocation

import spock.lang.Specification

class MockingAndBridgeMethods extends Specification {
  def "bridge methods are not intercepted"() {
    def comparator = Mock([:], MyComparator)

    when:
    comparator.compare((Object) 0, (Object) 0) // call bridge method

    then:
    1 * comparator.compare(0, 0) >> { IMockInvocation inv ->
      assert inv.getMethod().getParameterTypes() == [Integer, Integer]
    }
  }

  static class MyComparator implements Comparator<Integer> {
    int compare(Integer a, Integer b) {
      0
    }
  }
}


