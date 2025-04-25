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

import org.codehaus.groovy.control.MultipleCompilationErrorsException

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.SpockComparisonFailure
import org.spockframework.runtime.SpockMultipleFailuresError
import spock.lang.VerifyAll

class VerifyAllMethodsSpecification extends EmbeddedSpecification {

  def setup() {
    runner.throwFailure = false
  }

  @VerifyAll
  void isPositiveAndEven(int x) {
    x > 0
    x % 2 == 0
  }

  def "@VerifyAll method succeeds"() {
    expect:
    isPositiveAndEven(2)
  }

  def "@VerifyAll method fails"() {
    when:
    isPositiveAndEven(-3)

    then:
    SpockMultipleFailuresError e = thrown()
    with(e, SpockMultipleFailuresError) {
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

  def "@VerifyAll works on methods from non-spec helper classes"() {
    when:
    Assertions.isPositiveAndEven(-3)

    then:
    SpockMultipleFailuresError e = thrown()
    with(e, SpockMultipleFailuresError) {
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

  def "@VerifyAll can't be combined with @Verify standalone"() {
    when:
    runner.runWithImports '''
    class Helper {
      @Verify
      @VerifyAll
      void foo() {
        1 == 2
      }
    }
    '''

    then:
    MultipleCompilationErrorsException e = thrown()
    e.errorCollector.errors.message =~ [
        "Verification helper annotations can't be combined on 'foo', '@spock.lang.Verify' conflicts with '@spock.lang.VerifyAll'.",
        "Verification helper annotations can't be combined on 'foo', '@spock.lang.VerifyAll' conflicts with '@spock.lang.Verify'."
    ]
  }

  def "@VerifyAll can't be combined with @Verify within Specification"() {
    when:
    runner.runSpecBody'''
      @Verify
      @VerifyAll
      void foo() {
        1 == 2
      }
    '''

    then:
    MultipleCompilationErrorsException e = thrown()
    e.errorCollector.errors.message =~ [
        "Verification helper annotations can't be combined on 'foo', '@spock.lang.Verify' conflicts with '@spock.lang.VerifyAll'.",
        "Verification helper annotations can't be combined on 'foo', '@spock.lang.VerifyAll' conflicts with '@spock.lang.Verify'."
    ]
  }
}

class Assertions {
  @VerifyAll
  static void isPositiveAndEven(int x) {
    x > 0
    x % 2 == 0
  }
}
