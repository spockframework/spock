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

/**
 *
 * @author Peter Niederwieser
 */
class FailsWithOnMethod extends Specification {
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

