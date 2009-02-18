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

import org.spockframework.dsl.*
import static org.spockframework.dsl.Predef.*
import org.spockframework.runtime.ConditionNotSatisfiedError

/**
 * A ...
 *
 * @author Peter Niederwieser
 */
@Speck
@RunWith (Sputnik)
class ExplicitConditionsInFixtureMethods {
  @FailsWith(ConditionNotSatisfiedError)
  def setupSpeck() {
    assert false
  }

  @FailsWith(ConditionNotSatisfiedError)
  def cleanupSpeck() {
    assert false
  }

  @FailsWith(ConditionNotSatisfiedError)
  def setup() {
    assert false
  }

  @FailsWith(ConditionNotSatisfiedError)
  def cleanup() {
    assert false
  }

  def "dummy feature that triggers setup and cleanup"() {
    setup: def x = 0
  }
}