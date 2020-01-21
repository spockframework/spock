/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spock.util.concurrent

import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.SpockTimeoutError
import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.Specification

class PollingConditionsSpec extends Specification {
  PollingConditions conditions = new PollingConditions()
  def defConditions = new PollingConditions()

  volatile int num = 0
  volatile String str = null

  def "defaults"() {
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
      sleep(500)
      str = "hello"
    }

    when:
    conditions.eventually {
      num == 42
      str == "hello"
    }

    then:
    noExceptionThrown()
  }

  def "fails if any condition isn't satisfied in time"() {
    num = 42

    when:
    conditions.eventually {
      num == 42
      str == "bye"
    }

    then:
    thrown(SpockTimeoutError)
  }

  @Issue("https://github.com/spockframework/spock/issues/413")
  def "reports failed condition of last failed attempt"() {
    num = 42

    when:
    conditions.eventually {
      num == 42
      str == "bye"
    }

    then:
    SpockTimeoutError e = thrown()
    with(e.cause) {
      it instanceof ConditionNotSatisfiedError
      condition.text == 'str == "bye"'
    }
  }

  def "fails if condition is not met and assert keyword is used for def declared conditions object"() {
    num = 50

    when:
    defConditions.eventually {
      assert num == 42
    }

    then:
    thrown(SpockTimeoutError)
  }

  @PendingFeature(reason = "Known limitation")
  @Issue("https://github.com/spockframework/spock/issues/1054")
  def "fails if condition is not met and assert keyword is not used for def declared conditions object"() {
    num = 50

    when:
    defConditions.eventually {
      num == 42
    }

    then:
    thrown(SpockTimeoutError)
  }

  def "can override timeout per invocation"() {
    Thread.start {
      Thread.sleep(250)
      num = 42
    }

    when:
    conditions.within(0) {
      num == 42
    }

    then:
    thrown(SpockTimeoutError)
  }

  def "provides fine-grained control over polling rhythm"() {
    conditions.initialDelay = 0.01
    conditions.delay = 0.2
    conditions.factor = 2

    def count = 0
    def stats = [System.currentTimeMillis()]

    when:
    conditions.eventually {
      stats << System.currentTimeMillis()
      if (++count < 3) assert false
    }

    then:
    count == 3
    def spans = (1..3).collect { stats[it] - stats[it - 1] }
    spans[0] < spans[1]
    spans[1] < spans[2]
  }

  def "correctly handles checks that take longer than given check interval"() {
    when:
    def condition = new PollingConditions(timeout: 0.05)
    boolean secondAttempt = false
    then:
    condition.eventually {
      try {
        sleep 200;
        assert secondAttempt;
      } finally {
        secondAttempt = true
      }
    }
  }
}

