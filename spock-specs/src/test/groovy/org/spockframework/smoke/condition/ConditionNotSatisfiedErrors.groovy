package org.spockframework.smoke.condition

import spock.lang.Specification
import org.spockframework.runtime.ConditionNotSatisfiedError

class ConditionNotSatisfiedErrors extends Specification {
  def "each condition gets its own values (no undesired aliasing)"() {
    when:
    assert 1 == 2

    then:
    ConditionNotSatisfiedError e1 = thrown()

    when:
    assert 3 == 4

    then:
    ConditionNotSatisfiedError e2 = thrown()

    expect:
    e1.condition.values == [1, 2, false]
    e2.condition.values == [3, 4, false]
  }
}
