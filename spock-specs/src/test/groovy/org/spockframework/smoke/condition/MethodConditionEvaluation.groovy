
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

package org.spockframework.smoke.condition

import spock.lang.*

/**
 * Conditions that are method calls are treated specially
 * by Spock. Such calls are dispatched through SpockRuntime
 * rather than directly by Groovy. This spec checks that
 * arguments passed to condition methods are received correctly.
 *
 * Example for a condition that qualifies as method condition:
 * foo.bar(1, 2, 3)
 *
 * Counter example:
 * foo.bar(1, 2, 3) == 42
 */
class MethodConditionEvaluation extends Specification {
  Object[] expected

  def "pass object array with one 'regular' element"() {
    expected = [1] as Object[]

    expect:
    foo(expected)
  }

  def "pass object array with one null element"() {
    expected = [null] as Object[]

    expect:
    foo(expected)
  }

  def "pass object array with one list element"() {
    expected = [[1, 2, 3]] as Object[]

    expect:
    foo(expected)
  }

  private void foo(Object[] actual) {
    assert actual == expected
  }

  private void foo(Collection args) {
    assert false, "should never be called"
  }
}
