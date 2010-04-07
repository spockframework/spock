/*
 * Copyright 2010 the original author or authors.
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

import org.spockframework.runtime.SpockAssertionError
import org.spockframework.runtime.SpockTimeoutError

import spock.lang.*

class ConditionEvaluatorSpec extends Specification {
  def "passing example - check passes"() {
    def cond = new ConditionEvaluator()

    when:
    Thread.start {
      cond.evaluate {
        assert true
      }
    }

    then:
    cond.await()
  }

  @FailsWith(SpockAssertionError)
  def "failing example 1 - eval fails"() {
    def cond = new ConditionEvaluator()

    when:
    Thread.start {
      cond.evaluate {
        assert false
      }
    }

    then:
    cond.await()
  }

  @FailsWith(SpockTimeoutError)
  def "failing example 2 - check is missing"() {
    def cond = new ConditionEvaluator()

    expect:
    cond.await()
  }

  @FailsWith(SpockTimeoutError)
  def "one passing and one missing check"() {
    def conds = new ConditionEvaluator(2)

    when:
    Thread.start {
      conds.evaluate {
        assert true
      }
    }

    then:
    conds.await()
  }

  @FailsWith(SpockAssertionError)
  def "one failing and one missing check"() {
    def cond = new ConditionEvaluator(2)

    when:
    Thread.start {
      cond.evaluate {
        assert false
      }
    }

    then:
    cond.await()
  }

  def "multiple passing checks"() {
    def cond = new ConditionEvaluator(3)

    when:
    Thread.start {
      cond.evaluate {
        assert true
        assert true
      }
      cond.evaluate {
        assert true
      }
    }

    and:
    Thread.start {
      cond.evaluate {
        assert true
      }
    }

    then:
    cond.await()
  }
}
