/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke

import org.junit.runner.RunWith

import spock.lang.*
import static spock.lang.Predef.*
import org.spockframework.runtime.ConditionNotSatisfiedError

/**
 *
 * @author Peter Niederwieser
 */
@Speck
@RunWith (Sputnik)
class ExplicitConditionsInFeatureMethods {
  @FailsWith(ConditionNotSatisfiedError)
  def featureMethodAnonymousBlock() {
    assert false
    expect: true
  }

  @FailsWith(ConditionNotSatisfiedError)
  def featureMethodAnonymousBlockNested() {
    if (true) assert false
    expect: true
  }

  @FailsWith(ConditionNotSatisfiedError)
  def featureMethodAnonymousBlockNested2() {
    if (true) { { it -> assert false }() }
    expect: true
  }



  @FailsWith(ConditionNotSatisfiedError)
  def featureMethodSetupBlock() {
    setup: assert false
  }

  @FailsWith(ConditionNotSatisfiedError)
  def featureMethodSetupBlockNested() {
    setup: if (true) assert false
  }

  @FailsWith(ConditionNotSatisfiedError)
  def featureMethodExpectBlock() {
    expect: assert false
  }

  @FailsWith(ConditionNotSatisfiedError)
  def featureMethodExpectBlockNested() {
    expect: if (true) assert false
  }

  @FailsWith(ConditionNotSatisfiedError)
  def featureMethodWhenBlock() {
    when: assert false
    then: true
  }

  @FailsWith(ConditionNotSatisfiedError)
  def featureMethodWhenBlockNested() {
    when: if (true) assert false
    then: true
  }

  @FailsWith(ConditionNotSatisfiedError)
  def featureMethodThenBlock() {
    when: "nothing"
    then: assert false
  }

  @FailsWith(ConditionNotSatisfiedError)
  def featureMethodThenBlockNested() {
    when: "nothing"
    then: [1].each { assert false }
  }

  @FailsWith(ConditionNotSatisfiedError)
  def featureMethodCleanupBlock() {
    cleanup: assert false
  }

  @FailsWith(ConditionNotSatisfiedError)
  def featureMethodCleanupBlockNested() {
    cleanup: if (true) assert false
  }
}