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

import org.junit.runner.RunWith
import org.spockframework.util.SpockSyntaxException
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import spock.lang.*
import static spock.lang.Predef.*

/**
 * A ...

 * @author Peter Niederwieser
 */

@Speck
@RunWith(Sputnik)
class ExceptionConditions {
  def "basic usage"() {
    when: "".charAt(0)
    then: thrown(IndexOutOfBoundsException)
  }

  def "may occur after another condition"() {
    when: "".charAt(0)
    then: true; thrown(IndexOutOfBoundsException)
  }

  def "may occur in subsequent block"() {
    when: "".charAt(0)
    then: true
    and : thrown(IndexOutOfBoundsException)
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
    new GroovyClassLoader().parseClass("""
@spock.lang.Speck
class Ex {
  def ex() {
    when: throw new IOException()
    then:
      thrown(IOException)
      thrown(Exception)
  }
}
      """)

    then:
    MultipleCompilationErrorsException e = thrown()
    def error = e.errorCollector.getSyntaxError(0)
    error instanceof SpockSyntaxException
    error.line == 8
  }

  def "may only occur once in a group of then-blocks"() {
    when:
    new GroovyClassLoader().parseClass("""
@spock.lang.Speck
class Ex {
  def ex() {
    when: throw new IOException()
    then: thrown(IOException)
    and : thrown(Exception)
  }
}
      """)

    then:
    MultipleCompilationErrorsException e = thrown()
    def error = e.errorCollector.getSyntaxError(0)
    error instanceof SpockSyntaxException
    error.line == 7
  }
}