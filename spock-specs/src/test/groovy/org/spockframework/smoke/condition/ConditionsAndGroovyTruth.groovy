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
class SatisfiedConditions extends Specification {
  def "boolean"() {
    expect: true
  }

  def "number"() {
    expect: 1
  }

  def "object"() {
    expect: new Object()
  }

  def "collection"() {
    expect: [1]
  }

  def "++ and -- operators"() {
    def x = 1
    expect: x == 1
    and: x++
    and: x--
    and: ++x
    and: --x
    and: x == 1
  }

  def "involving operators and methods"() {
    expect: 3 * 5 + 4 == Math.abs(-7) + 12
  }
}

@FailsWith(ConditionNotSatisfiedError)
class UnsatisfiedConditions extends Specification {
  def "boolean"() {
    expect: false
  }

  def "number"() {
    expect: 0
  }

  def "object"() {
    expect: null
  }

  def "collection"() {
    expect: []
  }

  def "involving operators and methods"() {
    expect: 3 * 5 + 4 == Math.abs(-7) + 11
  }
}
