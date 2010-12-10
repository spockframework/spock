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

package spock.util.matcher

import org.spockframework.runtime.ConditionNotSatisfiedError

import spock.lang.*

import static spock.util.matcher.HamcrestMatchers.closeTo

class IsCloseToSpec extends Specification {
  def "compare Integers that are close enough"() {
    expect:
    3 closeTo(2, 1)
    3 closeTo(3, 0)
    3 closeTo(4, 1)
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "compare Integers that are not close enough"() {
    expect:
    3 closeTo(1, 1)
  }

  def "compare BigDecimals that are close enough"() {
    expect:
    3.1415 closeTo(3.1, 0.05)
    3.1415 closeTo(3.1415000, 0)
    3.1415 closeTo(3.2, 0.06)
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "compare BigDecimals that are not close enough"() {
    expect:
    3.1415 closeTo(3.0, 0.05)
  }

  def "mix integer and floating point numbers"() {
    expect:
    3.1415 closeTo(3, 0.15)
    3 closeTo(3.2, 1)
  }
}
