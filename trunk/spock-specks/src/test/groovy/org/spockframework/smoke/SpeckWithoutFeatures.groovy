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
 * A ...
 *
 * @author Peter Niederwieser
 */
// TODO: are we satisfied with this behavior?
// alternatives:
// - don't run setupSpeck()/cleanupSpeck()
// - run setup()/cleanup() once (might help to check whether they work or throw an exception)
@Speck
@RunWith (Sputnik)
class SpeckWithoutFeatures {
  @FailsWith(ConditionNotSatisfiedError)
  def setupSpeck() {
    assert false
  }

  @FailsWith(ConditionNotSatisfiedError)
  def cleanupSpeck() {
    assert false
  }

  def setup() {
    assert false, "not executed"
  }

  def cleanup() {
    assert false, "not executed"
  }
}