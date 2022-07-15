/*
 * Copyright 2010 the original author or authors.
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

import org.spockframework.runtime.SpockAssertionError
import org.spockframework.runtime.SpockTimeoutError

import spock.lang.*

class AsyncConditionsSpec extends Specification {
  def "passing example - check passes"() {
    def conds = new AsyncConditions()

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
  def "failing example 1 - eval fails"() {
    def conds = new AsyncConditions()

    when:
    Thread.start {
      conds.evaluate {
        assert false
      }
    }

    then:
    conds.await()
  }

  @FailsWith(SpockTimeoutError)
  def "failing example 2 - eval is missing"() {
    def conds = new AsyncConditions()

    expect:
    conds.await()
  }

  @FailsWith(SpockTimeoutError)
  def "one passing and one missing eval"() {
    def conds = new AsyncConditions(2)

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
  def "one failing and one missing eval"() {
    def conds = new AsyncConditions(2)

    when:
    Thread.start {
      conds.evaluate {
        assert false
      }
    }

    then:
    conds.await()
  }

  def "multiple passing evals"() {
    def conds = new AsyncConditions(3)

    when:
    Thread.start {
      conds.evaluate {
        assert true
        assert true
      }
      conds.evaluate {
        assert true
      }
    }

    and:
    Thread.start {
      conds.evaluate {
        assert true
      }
    }

    then:
    conds.await()
  }
}
