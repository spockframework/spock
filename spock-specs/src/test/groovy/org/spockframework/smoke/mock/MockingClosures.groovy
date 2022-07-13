/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.smoke.mock

import spock.lang.Specification

class MockingClosures extends Specification {
  def "can mock call() method"() {
    given:
    Closure c = Mock()

    when:
    c.call()

    then:
    1 * c.call()

    when:
    c.call("one")

    then:
    1 * c.call("one")

    when:
    c.call("one", "two")

    then:
    1 * c.call("one", "two")
  }
}
