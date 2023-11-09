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

package org.spockframework.smoke.condition

import org.spockframework.EmbeddedSpecification
import org.spockframework.compiler.InvalidSpecCompileException
import org.spockframework.runtime.InvalidSpecException
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.WrongExceptionThrownError
import org.spockframework.mock.InteractionNotSatisfiedError

import spock.lang.*

/**
 * @author Peter Niederwieser
 */
class ExceptionConditions extends EmbeddedSpecification {
  def "basic usage"() {
    when: "".charAt(0)
    then: thrown(IndexOutOfBoundsException)
  }

  def "may occur after another condition"() {
    when: "".charAt(0)
    then: true; thrown(IndexOutOfBoundsException)
  }

  def "may occur in and'ed block"() {
    when: "".charAt(0)
    then: true
    and : thrown(IndexOutOfBoundsException)
  }

  def "may occur in chained block"() {
    when: "".charAt(0)
    then: true
    then: thrown(IndexOutOfBoundsException)
  }

  def "when-block with var defs"() {
    when: "a when-block defines some vars and throws an exception"
    def x = null
    def ch = "".charAt(0)
    then: "the vars can be accessed from the then-block (provided the exception is caught)"
    ch == null
    thrown(IndexOutOfBoundsException)
    ch == x
  }

  def "catch an Exception"() {
    when: throw new IOException()
    then: thrown(IOException)
  }

  def "catch a RuntimeException"() {
    when: throw new IllegalArgumentException()
    then: thrown(IllegalArgumentException)
  }

  def "catch an Error"() {
    when: throw new ClassFormatError()
    then: thrown(ClassFormatError)
  }

  def "catch a Throwable"() {
    when: throw new Throwable()
    then: thrown(Throwable)
  }

  def "catch base type"() {
    when:
    "".charAt(0)
    then:
    Throwable t = thrown()
    t instanceof IndexOutOfBoundsException
  }

  def "exception in first block of when-group"() {
    when: throw new Exception()
    and : def x = 0
    then: thrown(Exception)
  }

  def "exception in second block of when-group"() {
    when: def x = 0
    and : throw new Exception()
    then: thrown(Exception)
  }

  def "multiple exceptions"() {
    when: throw new IOException()
    then: thrown(IOException)
    when: throw new IllegalArgumentException()
    then: thrown(IllegalArgumentException)
  }

  def "may only occur once in a then-block"() {
    when:
    compiler.compileFeatureBody """
when:
throw new IOException()

then:
thrown(IOException)
thrown(Exception)
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.line == 6
  }

  def "may only occur once in and'ed then-blocks"() {
    when:
    compiler.compileFeatureBody """
when: throw new IOException()
then: thrown(IOException)
and : thrown(Exception)
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.line == 3
  }

    def "may only occur once in chained then-blocks"() {
    when:
    compiler.compileFeatureBody """
when: throw new IOException()
then: thrown(IOException)
then: thrown(Exception)
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.line == 3
  }

  @Issue("https://github.com/spockframework/spock/issues/338")
  def "must be top-level statements"() {
    when:
    compiler.compileFeatureBody("""
def e = new Exception()

when:
throw e

then:
thrown() == e
    """)

    then:
    InvalidSpecCompileException e = thrown()
    e.message.startsWith("Exception conditions are only allowed as top-level statements")
  }

  def "thrown condition with undeterminable exception type"() {
    when:
    runner.runFeatureBody """
when:
true

then:
$thrownInteraction
    """

    then:
    InvalidSpecException e = thrown()
    e.message == "Thrown exception type cannot be inferred automatically. Please specify a type explicitly (e.g. 'thrown(MyException)')."

    where:
    thrownInteraction << [
      'thrown()',
      'thrown(null)',
      'def e = thrown(null)'
    ]
  }

  def "thrown condition with explicit type null and inferred type"() {
    when:
    throw new Exception()

    then:
    Exception e = thrown(null)
  }

  def "thrown condition with explicit and inferred type"() {
    when:
    runner.runFeatureBody """
when:
throw new Exception()

then:
Exception e = thrown(IOException)
    """

    then:
    WrongExceptionThrownError e = thrown()
    e.message == "Expected exception of type 'java.io.IOException', but got 'java.lang.Exception'"
  }

  @Issue("https://github.com/spockframework/spock/issues/260")
  @FailsWith(InvalidSpecException)
  def "(Java-style) exception condition must specify a type that is-a java.lang.Throwable"() {
    when:
    println()

    then:
    String str = thrown()
  }

  @Issue("https://github.com/spockframework/spock/issues/260")
  @FailsWith(InvalidSpecException)
  def "(Groovy-style) exception condition must specify a type that is-a java.lang.Throwable"() {
    when:
    println()

    then:
    thrown(String)
  }

  @Issue("https://github.com/spockframework/spock/issues/218")
  def "are able to observe a failing explicit condition"() {
    when:
    assert 1 * 2 == 3

    then:
    thrown(ConditionNotSatisfiedError)
  }

  @Issue("https://github.com/spockframework/spock/issues/218")
  @FailsWith(WrongExceptionThrownError)
  def "are able to observe a failing explicit condition (variation)"() {
    when:
    assert 1 * 2 == 3

    then:
    thrown(IOException)
  }

  @Issue("https://github.com/spockframework/spock/issues/218")
  @FailsWith(value = WrongExceptionThrownError, reason = "known limitation, not relevant in practice")
  def "are not able to observe a failing interaction"() {
    def list = Mock(List)
    1 * list.remove(1)

    when:
    list.add(1)

    then:
    thrown(InteractionNotSatisfiedError)
  }

  IOException ioException

  @FailsWith(WrongExceptionThrownError)
  def "can use field assignment rather than declaration (not encouraged)"() {
    when:
    throw new RuntimeException()

    then:
    ioException = thrown()
  }

  @FailsWith(WrongExceptionThrownError)
  def "can use variable assignment rather than declaration (not encouraged)"() {
    IOException ioException

    when:
    throw new RuntimeException()

    then:
    ioException = thrown()
  }

  def "can reuse exception variable declaration (not encouraged)"() {
    when:
    throw new RuntimeException()

    then:
    Exception e = thrown()

    when:
    throw new IOException()

    then:
    e = thrown()
  }

  @Issue("https://github.com/spockframework/spock/issues/419")
  def "can have multi-assignments in when-block"() {
    when:
    def (x, y) = [1, 2]
    (x, y) = [3, 4]
    throw new RuntimeException()
    (x, y) = [5, 6]

    then:
    thrown(RuntimeException)
    x == 3
    y == 4
  }

  @Issue("https://github.com/spockframework/spock/issues/1266")
  def "thrown conditions don't destroy method reference when invocation is assigned to variable with the same name"() {
    when:
    def foobar = foobar()

    then:
    thrown(IllegalStateException)
  }

  @Issue("https://github.com/spockframework/spock/issues/1332")
  def "thrown conditions don't destroy method reference when invocation is assigned to a multi-assignment with the same name"() {
    when:
    def (foobar, b) = foobar()

    then:
    thrown(Exception)
  }

  def foobar() {
    throw new IllegalStateException("foo")
  }

}
