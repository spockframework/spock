/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import spock.lang.FailsWith

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

  @FailsWith(InvalidSpecException)
  def "(Java-style) exception condition must specify a type that is-a java.lang.Throwable"() {
    when:
    println()

    then:
    String str = thrown()
  }

  @FailsWith(InvalidSpecException)
  def "(Groovy-style) exception condition must specify a type that is-a java.lang.Throwable"() {
    when:
    println()

    then:
    thrown(String)
  }
}