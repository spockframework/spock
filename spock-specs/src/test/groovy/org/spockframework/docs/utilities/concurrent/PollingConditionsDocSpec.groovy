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

    when:
    Thread.start {
      num = 42
      sleep(25)          // <2>
      str = "hello"
    }

    then:
    conditions.eventually {          // <3>
      num == 42
    }

    and:
    conditions.eventually {          // <3>
      str == "hello"
    }

    and:
    noExceptionThrown()          // <4>
  }

  def "fails if any condition isn't satisfied in time"() {

    given:
    Thread.start {
      num = 42
      sleep(25) // milliseconds     <2>
      str = "hello"
    }

    expect:
    conditions.within(0.05) { // seconds     <5>
      num == 42
    }

    when:
    conditions.eventually {          // <3>
      num == 0
      str == "bye"
    }

    then:
    def error = thrown(SpockTimeoutError)
    error.cause.message.contains('num == 0')          // <6>
  }
  // end::polling-conditions-spec[]
}
