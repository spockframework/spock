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

package org.spockframework.smoke.condition

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
class ExplicitConditionsInHelperMethods {
  def topLevel() {
    assert false
  }

  def nested() {
    if (true) assert false
  }

  def deeplyNested() {
    if (true) {
      { it -> assert false }()
    }
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "top-level explicit conditions in helper methods should work"() {
    setup: topLevel()
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "nested explicit conditions in helper methods should work"() {
    setup: nested()
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "deeply nested explicit conditions in helper methods should work"() {
    setup: deeplyNested()
  }
}