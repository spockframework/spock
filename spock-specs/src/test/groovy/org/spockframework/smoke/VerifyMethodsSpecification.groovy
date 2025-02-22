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

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.ConditionNotSatisfiedError

class VerifyMethodsSpecification extends EmbeddedSpecification {

  def "@Verify method succeeds"() {
    when:
    def result = runner.runSpecBody '''
@Verify
private static void isPositive(int x) {
    x > 0
}

def "feature"() {
    expect:
    isPositive(2)
}
'''

    then:
    result.failures.empty
  }

  def "@Verify method fails"() {
    when:
    runner.runSpecBody '''
@Verify
private static void isPositive(int x) {
    x > 0
}

def "feature"() {
    expect:
    isPositive(-2)
}
'''

    then:
    ConditionNotSatisfiedError e = thrown()
    e.message == '''\
Condition not satisfied:

x > 0
| |
| false
-2
'''
  }

  def "uses @Verify methods from non-spec helper classes"() {
    when:
    runner.runWithImports '''
class Assertions {
  @Verify
  static void isPositive(int x) {
      x > 0
  }
}

class SpecWithHelpers extends Specification {
  def "feature"() {
      expect:
      Assertions.isPositive(-2)
  }
}
'''

    then:
    ConditionNotSatisfiedError e = thrown()
    e.message == '''\
Condition not satisfied:

x > 0
| |
| false
-2
'''
  }

  def "first failing assertion fails the feature"() {
    when:
    runner.runWithImports '''
class Assertions {
  @Verify
  static void isPositiveAndEven(int x) {
      x > 0
      x % 2 == 0
  }
}

class SpecWithHelpers extends Specification {
  def "feature"() {
      expect:
      Assertions.isPositiveAndEven(-2)
  }
}
'''

    then:
    ConditionNotSatisfiedError e = thrown()
    e.message == '''\
Condition not satisfied:

x > 0
| |
| false
-2
'''
  }
}
