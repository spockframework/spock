/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.ConditionFailedWithExceptionError
import org.spockframework.runtime.InvalidSpecException
import spock.lang.FailsWith
import spock.lang.Specification

import org.spockframework.runtime.SpockComparisonFailure

/**
 *
 * @author Peter Niederwieser
 */
class FailsWithOnMethod extends EmbeddedSpecification {
  @FailsWith(IndexOutOfBoundsException)
  def ex1() {
    given:
    def foo = []
    foo.get(0)
  }

  @FailsWith(Exception)
  def ex2() {
    given:
    def foo = []
    foo.get(0)
  }

  def ex3() {
    expect: true
  }

  @FailsWith(
      value = RuntimeException,
      expectedMessage = "My message"
  )
  def withMessage() {
    given:
    throw new RuntimeException("My message")
  }

  def "@FailsWith can assert exception message"() {
    when:
    runner.runSpecBody """
  @FailsWith(
      value = RuntimeException,
      expectedMessage = "My message"
  )
  def foo() {
    given:
    throw new RuntimeException("Not my message")
  }
    """

    then:
    SpockComparisonFailure e = thrown()
    def expected = """Condition not satisfied:

e.value == expectedMessage
| |        |
| |        My message
| Not my message
java.lang.RuntimeException: Not my message"""

    e.message.startsWith(expected)

  }

  @FailsWith(ConditionFailedWithExceptionError)
  def "can handle ConditionFailedWithExceptionError"() {
    when:
    def a = [b:null]

    then:
    a.b.c.size()
  }

  @FailsWith(NullPointerException)
  def "can handle cause of ConditionFailedWithExceptionError"() {
    when:
    def a = [b:null]

    then:
    a.b.c.size()
  }
}

@FailsWith(IndexOutOfBoundsException)
class FailsWithOnSpec extends Specification {
  def ex1() {
    given:
    def foo = []
    foo.get(0)
  }

  def ex2() {
    given:
    def foo = []
    foo.get(1)
  }

  @FailsWith(NullPointerException)
  def ex3() {
    given:
    null.foo()
  }
}

class FailsWithMisuse extends EmbeddedSpecification {
  def "@FailsWith on method can only refer to exception type"() {
    when:
    runner.runSpecBody """
@FailsWith(List)
def foo() {
  expect: true
}
    """

    then:
    InvalidSpecException e = thrown()
    e.message == "@FailsWith needs to refer to an exception type, but does refer to 'java.util.List'"
  }

  def "@FailsWith on class can only refer to exception type"() {
    when:
    runner.runWithImports """
@FailsWith(List)
class MySpec extends Specification {
  def foo() {
    expect: true
  }
}
    """

    then:
    InvalidSpecException e = thrown()
    e.message == "@FailsWith needs to refer to an exception type, but does refer to 'java.util.List'"
  }
}
