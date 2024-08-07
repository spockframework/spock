/*
 * Copyright 2024 the original author or authors.
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
package org.spockframework.mock.response

import spock.lang.Issue
import spock.lang.Specification

class MockPrimitiveTypeResponsesSpec extends Specification {
  private static final String SUCCESS = "success"

  @Issue("https://github.com/spockframework/spock/issues/1974")
  def "Mock Response TypeArgumentConstraint with primitive type Issue #1974"() {
    given:
    ObjClass client = Mock()

    when: "invoke with int"
    def response = client.test(123, 456)
    then: "validate with int TypeArgumentConstraint (as int)"
    1 * client.test(_ as int, _ as int) >> SUCCESS
    response == SUCCESS
  }

  @Issue("https://github.com/spockframework/spock/issues/1974")
  def "Mock Response TypeArgumentConstraint with different primitive types Issue #1974"() {
    given:
    ObjClass client = Mock()

    when: "invoke with other instance"
    def response = client.test(123d, false)
    then: "validate with int TypeArgumentConstraint (as int)"
    0 * client.test(_ as int, _ as int)
    response == null
  }

  static class ObjClass {
    @SuppressWarnings('GrMethodMayBeStatic')
    String test(int a, int b) {
      return a + b
    }
  }
}
