/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.condition

import org.spockframework.runtime.ConditionNotSatisfiedError
import spock.lang.FailsWith
import spock.lang.Specification

/**
 * @author Peter Niederwieser
 */
class ExplicitConditionsInNestedPositions extends Specification {
  @FailsWith(ConditionNotSatisfiedError)
  def "in for loop"() {
    setup:
    for (i in 1..3) assert false
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "in closure"() {
    setup:
    [1, 2, 3].each { assert false }
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "in implicit condition"() {
    expect:
    [1, 2, 3].every { assert false }
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "in void method in expect block"() {
    expect:
    [1, 2, 3].each { assert false }
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "deeply nested"() {
    expect:
    [1, 2, 3].every { if (true) [1].any { assert false } }
  }
}
