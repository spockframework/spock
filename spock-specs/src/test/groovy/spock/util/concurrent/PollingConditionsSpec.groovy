/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spock.util.concurrent

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.GroovyRuntimeUtil
import org.spockframework.runtime.SpockTimeoutError
import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.PendingFeatureIf
import spock.lang.Requires
import spock.lang.Retry

class PollingConditionsSpec extends EmbeddedSpecification {
  PollingConditions conditions = new PollingConditions()

  private static def noArgClosure = { "test" }
  private static def throwableArgClosure = { Throwable err -> err.getClass().simpleName }

  def setup() {
    runner.addClassImport(PollingConditions)
  }

  def "defaults"() {
    expect:
    with(conditions) {
      timeout == 1
      initialDelay == 0
      delay == 0.1
      factor == 1
    }
  }

  @PendingFeatureIf(value = { !data.conditionMethod }, reason = "Known limitation")
  @Retry
  def "succeeds if all conditions are eventually satisfied with condition method '#conditionMethod' and field"() {
    when:
    runner.runSpecBody """
      PollingConditions conditions = new PollingConditions()

      volatile int num = 0
      volatile String str = null

      def 'a feature'() {
        num = 42
        Thread.start {
          sleep(500)
          str = "hello"
        }

        expect:
        conditions$conditionMethod {
          num == 42
          str == "hello"
        }
      }
    """

    then:
    noExceptionThrown()

    where:
    conditionMethod << [".eventually", ".call", ""]
  }

  @Retry
  def "succeeds if all conditions are eventually satisfied with condition method '#conditionMethod' and local variable"() {
    when:
    runner.runSpecBody """
      volatile int num = 0
      volatile String str = null

      def 'a feature'() {
        PollingConditions conditions = new PollingConditions()
        num = 42
        Thread.start {
          sleep(500)
          str = "hello"
        }

        expect:
        conditions$conditionMethod {
          num == 42
          str == "hello"
        }
      }
    """

    then:
    noExceptionThrown()

    where:
    conditionMethod << [".eventually", ".call", ""]
  }

  @PendingFeatureIf(value = { !data.conditionMethod }, reason = "Known limitation")
  def "fails if any condition isn't satisfied in time with condition method '#conditionMethod' and field"() {
    when:
    runner.runSpecBody """
      PollingConditions conditions = new PollingConditions()

      volatile int num = 0
      volatile String str = null

      def 'a feature'() {
        num = 42

        expect:
        conditions$conditionMethod {
          num == 42
          str == "bye"
        }
      }
    """

    then:
    thrown(SpockTimeoutError)

    where:
    conditionMethod << [".eventually", ".call", ""]
  }

  def "fails if any condition isn't satisfied in time with condition method '#conditionMethod' and local variable"() {
    when:
    runner.runSpecBody """
      volatile int num = 0
      volatile String str = null

      def 'a feature'() {
        PollingConditions conditions = new PollingConditions()
        num = 42

        expect:
        conditions$conditionMethod {
          num == 42
          str == "bye"
        }
      }
    """

    then:
    thrown(SpockTimeoutError)

    where:
    conditionMethod << [".eventually", ".call", ""]
  }

  @PendingFeatureIf(value = { !data.conditionMethod }, reason = "Known limitation")
  @Issue("https://github.com/spockframework/spock/issues/413")
  def "reports failed condition of last failed attempt with condition method '#conditionMethod' and field"() {
    when:
    runner.runSpecBody """
      PollingConditions conditions = new PollingConditions()

      volatile int num = 0
      volatile String str = null

      def 'a feature'() {
        num = 42

        expect:
        conditions$conditionMethod {
          num == 42
          str == "bye"
        }
      }
    """

    then:
    SpockTimeoutError e = thrown()
    with(e.cause) {
      it instanceof ConditionNotSatisfiedError
      condition.text == 'str == "bye"'
    }

    where:
    conditionMethod << [".eventually", ".call", ""]
  }

  @Issue("https://github.com/spockframework/spock/issues/413")
  def "reports failed condition of last failed attempt with condition method '#conditionMethod' and local variable"() {
    when:
    runner.runSpecBody """
      volatile int num = 0
      volatile String str = null

      def 'a feature'() {
        PollingConditions conditions = new PollingConditions()
        num = 42

        expect:
        conditions$conditionMethod {
          num == 42
          str == "bye"
        }
      }
    """

    then:
    SpockTimeoutError e = thrown()
    with(e.cause) {
      it instanceof ConditionNotSatisfiedError
      condition.text == 'str == "bye"'
    }

    where:
    conditionMethod << [".eventually", ".call", ""]
  }

  @Requires({ (GroovyRuntimeUtil.MAJOR_VERSION >= 3) || data.conditionMethod })
  def "fails if condition is not met and assert keyword is used for def declared conditions object with condition method '#conditionMethod' and field"() {
    when:
    runner.runSpecBody """
      def defConditions = new PollingConditions()
      volatile int num = 0

      def 'a feature'() {
        num = 50

        expect:
        defConditions$conditionMethod {
          assert num == 42
        }
      }
    """

    then:
    thrown(SpockTimeoutError)

    where:
    conditionMethod << [".eventually", ".call", ""]
  }

  @Requires({ (GroovyRuntimeUtil.MAJOR_VERSION >= 3) || data.conditionMethod })
  def "fails if condition is not met and assert keyword is used for def declared conditions object with condition method '#conditionMethod' and local variable"() {
    when:
    runner.runSpecBody """
      def defConditions = new PollingConditions()
      volatile int num = 0

      def 'a feature'() {
        num = 50

        expect:
        defConditions$conditionMethod {
          assert num == 42
        }
      }
    """

    then:
    thrown(SpockTimeoutError)

    where:
    conditionMethod << [".eventually", ".call", ""]
  }

  @PendingFeature(reason = "Known limitation")
  @Requires({ (GroovyRuntimeUtil.MAJOR_VERSION >= 3) || data.conditionMethod })
  @Issue("https://github.com/spockframework/spock/issues/1054")
  def "fails if condition is not met and assert keyword is not used for def declared conditions object with condition method '#conditionMethod' and field"() {
    when:
    runner.runSpecBody """
      def defConditions = new PollingConditions()
      volatile int num = 0

      def 'a feature'() {
        num = 50

        expect:
        defConditions$conditionMethod {
          num == 42
        }
      }
    """

    then:
    thrown(SpockTimeoutError)

    where:
    conditionMethod << [".eventually", ".call", ""]
  }

  @PendingFeature(reason = "Known limitation")
  @Requires({ (GroovyRuntimeUtil.MAJOR_VERSION >= 3) || data.conditionMethod })
  @Issue("https://github.com/spockframework/spock/issues/1054")
  def "fails if condition is not met and assert keyword is not used for def declared conditions object with condition method '#conditionMethod' and local variable"() {
    when:
    runner.runSpecBody """
      def defConditions = new PollingConditions()
      volatile int num = 0

      def 'a feature'() {
        num = 50

        expect:
        defConditions$conditionMethod {
          num == 42
        }
      }
    """

    then:
    thrown(SpockTimeoutError)

    where:
    conditionMethod << [".eventually", ".call", ""]
  }

  @PendingFeatureIf(value = { !data.conditionMethod }, reason = "Known limitation")
  @Requires({ (GroovyRuntimeUtil.MAJOR_VERSION >= 3) || data.conditionMethod })
  def "can override timeout per invocation with condition method '#conditionMethod' and field"() {
    when:
    runner.runSpecBody """
      PollingConditions conditions = new PollingConditions()

      volatile int num = 0

      def 'a feature'() {
        Thread.start {
          Thread.sleep(250)
          num = 42
        }

        expect:
        conditions$conditionMethod(0) {
          num == 42
        }
      }
    """

    then:
    thrown(SpockTimeoutError)

    where:
    conditionMethod << [".within", ".call", ""]
  }

  @Requires({ (GroovyRuntimeUtil.MAJOR_VERSION >= 3) || data.conditionMethod })
  def "can override timeout per invocation with condition method '#conditionMethod' and local variable"() {
    when:
    runner.runSpecBody """
      volatile int num = 0

      def 'a feature'() {
        PollingConditions conditions = new PollingConditions()
        Thread.start {
          Thread.sleep(250)
          num = 42
        }

        expect:
        conditions$conditionMethod(0) {
          num == 42
        }
      }
    """

    then:
    thrown(SpockTimeoutError)

    where:
    conditionMethod << [".within", ".call", ""]
  }

  @Retry
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
    given: "polling condition with small timeout"
    def condition = new PollingConditions(timeout: 0.05)
    boolean secondAttempt = false

    when: "first attempt already takes longer than the timeout"
    condition.eventually {
      try {
        sleep 100
        assert secondAttempt
      } finally {
        secondAttempt = true
      }
    }

    then: "there will be no second one"
    thrown SpockTimeoutError
  }

  def "correctly creates timeout error message"() {
    given:
    PollingConditions conditions = new PollingConditions()
    conditions.onTimeout(onTimeoutClosure)

    when:
    conditions.eventually {
      1 == 0
    }

    then:
    def e = thrown(SpockTimeoutError)
    e.message ==~ /Condition not satisfied after \d+(\.\d+)? seconds and \d+ attempts/ + expectedMessageSuffix

    where:
    onTimeoutClosure    || expectedMessageSuffix
    null                || ""
    noArgClosure        || ": test"
    throwableArgClosure || ": ConditionNotSatisfiedError"
  }

  def "correctly creates timeout error message when onTimeout called multiple times"() {
    given:
    PollingConditions conditions = new PollingConditions()
    conditions.onTimeout(onTimeoutClosure)
    conditions.onTimeout(secondOnTimeoutClosure)

    when:
    conditions.eventually {
      1 == 0
    }

    then:
    def e = thrown(SpockTimeoutError)
    e.message ==~ /Condition not satisfied after \d+(\.\d+)? seconds and \d+ attempts/ + expectedMessageSuffix

    where:
    onTimeoutClosure    | secondOnTimeoutClosure || expectedMessageSuffix
    noArgClosure        | null                   || ""
    null                | noArgClosure           || ": test"
    throwableArgClosure | noArgClosure           || ": test"
  }
}
