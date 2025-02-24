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

import org.opentest4j.MultipleFailuresError
import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.SpockComparisonFailure

class VerifyAllMethodsSpecification extends EmbeddedSpecification {

  def setup() {
    runner.throwFailure = false
  }

  def "@VerifyAll method succeeds"() {
    when:
    def result = runner.runSpecBody '''
@VerifyAll
private static void isPositiveAndEven(int x) {
    x > 0
}

def "feature"() {
    expect:
    isPositiveAndEven(2)
}
'''

    then:
    result.failures.empty
  }

  def "@VerifyAll method fails"() {
    when:
    def result = runner.runSpecBody '''
@VerifyAll
private static void isPositiveAndEven(int x) {
    x > 0
    x % 2 == 0
}

def "feature"() {
    expect:
    isPositiveAndEven(-3)
}
'''

    then:
    result.failures.size() == 1
    with(result.failures[0].exception, MultipleFailuresError) {
      failures.size() == 2
      with(failures[0], ConditionNotSatisfiedError) {
        condition.text == 'x > 0'
      }
      with(failures[1], SpockComparisonFailure) {
        expected.stringRepresentation.trim() == "0"
        actual.stringRepresentation.trim() == "-1"
      }
    }
  }

  def "uses @VerifyAll methods from non-spec helper classes"() {
    when:
    def result = runner.runWithImports '''
class Assertions {
  @VerifyAll
  static void isPositiveAndEven(int x) {
      x > 0
      x % 2 == 0
  }
}

class SpecWithHelpers extends Specification {
  def "feature"() {
      expect:
      Assertions.isPositiveAndEven(-3)
  }
}
'''

    then:
    result.failures.size() == 1
    with(result.failures[0].exception, MultipleFailuresError) {
      failures.size() == 2
      with(failures[0], ConditionNotSatisfiedError) {
        condition.text == 'x > 0'
      }
      with(failures[1], SpockComparisonFailure) {
        expected.stringRepresentation.trim() == "0"
        actual.stringRepresentation.trim() == "-1"
      }
    }
  }
}
