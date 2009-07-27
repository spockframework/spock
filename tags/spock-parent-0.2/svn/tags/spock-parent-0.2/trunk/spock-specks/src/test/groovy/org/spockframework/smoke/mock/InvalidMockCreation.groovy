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

package org.spockframework.smoke.mock

import org.junit.runner.RunWith

import spock.lang.*
import static spock.lang.Predef.*
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.spockframework.util.SpockSyntaxException

/**
 *
 * @author Peter Niederwieser
 */
@Speck
@RunWith (Sputnik)
class InvalidMockCreation {
  def "field w/ missing type"() {
    when:
    new GroovyClassLoader().parseClass("""
import spock.lang.Speck
@Speck
class Foo {
  def list = Mock()
}
    """)

    then:
    MultipleCompilationErrorsException e = thrown()
    e.errorCollector.errors[0].cause instanceof SpockSyntaxException
  }

  // for "field w/ incompatible type" and "field w/ wrong argument" see Specks below

  def "local w/ missing type"() {
    when:
    new GroovyClassLoader().parseClass("""
import spock.lang.Speck
@Speck
class Foo {
  def foo() {
    setup:
    def list = Mock()
  }
}
    """)

    then:
    MultipleCompilationErrorsException e = thrown()
    e.errorCollector.errors[0].cause instanceof SpockSyntaxException
  }

  def "local w/ incompatible type"() {
    when: List list = Mock(Map)
    then: thrown(GroovyCastException)
  }

  def "local w/ wrong argument"() {
    when: def list = Mock(1)
    then: thrown(MissingMethodException)
  }

  def "expr w/ missing type"() {
    when:
    new GroovyClassLoader().parseClass("""
import spock.lang.Speck
@Speck
class Foo {
  def foo() {
    setup: Mock()
  }
}
    """)

    then:
    MultipleCompilationErrorsException e = thrown()
    e.errorCollector.errors[0].cause instanceof SpockSyntaxException
  }

  def "expr w/ wrong argument"() {
    when: Mock(1)
    then: thrown(MissingMethodException)
  }
}

@Speck
@RunWith (Sputnik)
class InvalidMockCreation2 {
  List list = Mock(Map)

  @FailsWith(GroovyCastException)
  def setup() {}

  def "field w/ incompatible type"() {
    expect: true // triggers setup()
  }
}

@Speck
@RunWith (Sputnik)
class InvalidMockCreation3 {
  def list = Mock(1)

  @FailsWith(MissingMethodException)
  def setup() {}

  def "field w/ wrong argument"() {
    expect: true // triggers setup()
  }
}