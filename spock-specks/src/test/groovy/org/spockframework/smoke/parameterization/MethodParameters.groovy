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

package org.spockframework.smoke.parameterization

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import spock.util.EmbeddedSpecification
import org.spockframework.util.SpockSyntaxException

/**
 * @author Peter Niederwieser
 */
class MethodParameters extends EmbeddedSpecification {
  def "no parameters"() {
    expect:
    x == y

    where:
    x << [1, 2]
    y << [1, 2]
  }

  def "parameters but no where block"() {
    when:
    compiler.compileSpeckBody """
def foo(int x, String y) {
  expect: true
}
    """

    then:
    Exception e = thrown()
    containsSyntaxException(e)
  }

  def "typed parameters"(Integer x, Integer y) {
    expect:
    x == y

    where:
    x << [1, 2]
    y << [1, 2]
  }

  def "typed primitive parameters"(int x, int y) {
    expect:
    x == y

    where:
    x << [1, 2]
    y << [1, 2]
  }

  def "untyped parameters"(x, y) {
    expect:
    x == y

    where:
    x << [1, 2]
    y << [1, 2]
  }

  def "partly typed parameters"(x, Integer y) {
    expect:
    x == y

    where:
    x << [1, 2]
    y << [1, 2]
  }

  def "parameters in different order"(y, x) {
    expect:
    x == y

    where:
    x << [1, 2]
    y << [1, 2]
  }

  def "fewer parameters than data variables"() {
    when:
    compiler.compileSpeckBody """
def foo(x) {
  expect:
  x == y

  where:
  x << [1, 2]
  y << [1, 2]
}
    """

    then:
    Exception e = thrown()
    containsSyntaxException(e)
  }


  def "more parameters than data variables"() {
    when:
    compiler.compileSpeckBody """
def foo(x, y, z) {
  expect:
  x == y

  where:
  x << [1, 2]
  y << [1, 2]
}
    """

    then:
    Exception e = thrown()
    containsSyntaxException(e)
  }

  def "parameter names do not match data variable names"() {
    when:
    compiler.compileSpeckBody """
def foo(x, a) {
  expect:
  x == y

  where:
  x << [1, 2]
  y << [1, 2]
}
    """

    then:
    Exception e = thrown()
    containsSyntaxException(e)
  }

  def "data value type can be coerced to parameter type"(x, String y) {
    expect:
    x.toString() == y

    where:
    x << [1, 2]
    y << [1, 2]
  }

  def "data value type can not be coerced to parameter type"() {
    when:
    runner.runSpeckBody """
def foo(x, ClassLoader y) {
  expect:
  x == y

  where:
  x << [1, 2]
  y << [1, 2]
}
    """

    then:
    thrown(GroovyCastException)
  }

  void containsSyntaxException(e) {
    assert e instanceof MultipleCompilationErrorsException
    assert e.errorCollector.errorCount == 1
    assert e.errorCollector.errors[0].cause instanceof SpockSyntaxException
  }
}