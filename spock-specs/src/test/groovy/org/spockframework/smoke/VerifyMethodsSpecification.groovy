/*
 * Copyright 2025 the original author or authors.
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

package org.spockframework.smoke

import org.spockframework.runtime.ConditionNotSatisfiedError
import spock.lang.FailsWith
import spock.lang.Specification
import spock.lang.Verify

class VerifyMethodsSpecification extends Specification {

  @Verify
  private static isPositive(int x) {
    [true, false].any { it }
    x > 0
  }

  private static class Assertions {
    @Verify
    private static isPositive(int x) {
      x > 0
    }

    @Verify
    private static isPositiveAndEven(int x) {
      x > 0
      x % 2 == 0
    }
  }

  def "@Verify method succeeds"() {
    expect:
    isPositive(2)
  }

  @FailsWith(
    value = ConditionNotSatisfiedError,
    expectedMessage = '''\
Condition not satisfied:

x > 0
| |
| false
-2
''')
  def "@Verify method fails"() {
    expect:
    isPositive(-2)
  }

  @FailsWith(
    value = ConditionNotSatisfiedError,
    expectedMessage = '''\
Condition not satisfied:

x > 0
| |
| false
-2
''')
  def "@Verify works on methods from non-spec helper classes"() {
    expect:
    Assertions.isPositive(-2)
  }

  @FailsWith(
    value = ConditionNotSatisfiedError,
    expectedMessage = '''\
Condition not satisfied:

x > 0
| |
| false
-2
''')
  def "first failing assertion fails the feature"() {
    expect:
    Assertions.isPositiveAndEven(-2)
  }
}
