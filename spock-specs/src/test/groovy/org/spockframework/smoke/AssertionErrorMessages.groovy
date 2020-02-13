
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

package org.spockframework.smoke

import org.spockframework.runtime.SpockAssertionError

import spock.lang.*

@Issue("https://github.com/spockframework/spock/issues/217")
class AssertionErrorMessages extends Specification {
  def "assertion error returns (same) message for getMessage() and toString()"() {
    def e = createSpockAssertionError()

    expect:
    e.message
    e.message.contains("1 == 2")
    e.message == e.toString()
  }

  def createSpockAssertionError() {
    try {
      assert 1 == 2
    } catch (SpockAssertionError e) {
      return e
    }
  }
}
