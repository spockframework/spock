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

package org.spockframework.smoke

import spock.lang.Specification
import spock.lang.FailsWith

import org.spockframework.runtime.ConditionNotSatisfiedError

class WithBlockFailingConditions extends Specification {
  @FailsWith(ConditionNotSatisfiedError)
  def "basic usage"() {
    def list = [1, 2]

    expect:
    with(list) {
      size() == 3
    }
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "inlining"() {
    expect:
    with([1, 2]) {
      size() == 3
    }
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "nesting"() {
    def list = [1, 2]

    expect:
    with(list) {
      size() == 2
      get(0) == 1

      def map = [foo: "bar"]
      with(map) {
        size() == 2
      }
    }
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "in then-block"() {
    when:
    def list = [1, 2]

    then:
    with(list) {
      size() == 3
    }
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "in nested position"() {
    when:
    def list = [1, 2]

    then:
    1.times {
      1.times {
        with(list) {
          size() == 3
        }
      }
    }
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "executed several times"() {
    when:
    def list = [1, 2]

    then:
    3.times {
      with(list) {
        size() == 3
      }
    }
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "in block other than then/expect"() {
    def list = [1, 2]

    setup:
    with(list) {
      size() == 3
    }
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "in helper method"() {
    def list = [1, 2]

    expect:
    helper(list)
  }

  void helper(list) {
    with(list) {
      size() == 3
    }
  }
}
