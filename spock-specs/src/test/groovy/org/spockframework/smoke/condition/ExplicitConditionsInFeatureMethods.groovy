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
import spock.lang.Issue

/**
 * @author Peter Niederwieser
 */
class ExplicitConditionsInFeatureMethods extends Specification {
  @FailsWith(ConditionNotSatisfiedError)
  def "in anonymous block"() {
    assert false
    expect: true
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "nested in anonymous block"() {
    if (true) assert false
    expect: true
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "in setup block"() {
    setup: assert false
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "nested in setup block"() {
    setup: if (true) assert false
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "in expect block"() {
    expect: assert false
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "nested in expect block"() {
    expect: if (true) assert false
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "in when block"() {
    when: assert false
    then: true
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "nested in when block"() {
    when: if (true) assert false
    then: true
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "in then block"() {
    when: "nothing"
    then: assert false
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "nested in then block"() {
    when: "nothing"
    then: [1].each { assert false }
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "in cleanup block"() {
    cleanup: assert false
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "nested in cleanup block"() {
    cleanup: if (true) assert false
  }

  // This may need a more suitable home
  @Issue("https://github.com/spockframework/spock/issues/270")
  def "assert expression contains method call"() {
    expect: assert "test".toString() : "dummy text"
  }

}
