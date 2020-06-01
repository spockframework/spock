package org.spockframework.docs.utilities.concurrent

import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.SpockTimeoutError
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class PollingConditionsDocSpec extends Specification {
  // tag::polling-conditions-spec[]
  PollingConditions conditions = new PollingConditions()          // <1>

  volatile int num = 0
  volatile String str = null

  def "time controls and their default values"() {
    expect:
    with(conditions) {
      timeout == 1
      initialDelay == 0
      delay == 0.1
      factor == 1
    }
  }

  def "succeeds if all conditions are eventually satisfied"() {
    num = 42
    Thread.start {
      sleep(500)          // <2>
      str = "hello"
    }

    when:
    conditions.eventually {          // <3>
      num == 42
      str == "hello"
    }

    then:
    noExceptionThrown()          // <4>
  }

  def "fails if any condition isn't satisfied in time"() {
    num = 42

    when:
    conditions.eventually {
      num == 42
      str == "bye"
    }

    then:
    def error = thrown(SpockTimeoutError)
    error.cause instanceof ConditionNotSatisfiedError          // <5>
  }
  // end::polling-conditions-spec[]
}
