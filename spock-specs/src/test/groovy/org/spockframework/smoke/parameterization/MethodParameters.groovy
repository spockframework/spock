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

import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.SpockExecutionException
import spock.lang.FailsWith
import spock.lang.Issue
import spock.lang.Rollup
import spock.lang.Unroll

/**
 * @author Peter Niederwieser
 */
@Rollup
class MethodParameters extends EmbeddedSpecification {
  def "no parameters"() {
    expect:
    x == y

    where:
    x << [1, 2]
    y << [1, 2]
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

  @Issue("https://github.com/spockframework/spock/issues/652")
  def "fewer parameters than data variables"(x) {
    expect:
    x == y

    where:
    x << [1, 2]
    y << [1, 2]
  }

  @Issue("https://github.com/spockframework/spock/issues/652")
  def "more parameters than data variables throw exception if not injected by some extension"() {
    when:
    runner.runSpecBody '''
def foo(x, y, z) {
  expect:
  x == y
  z == null

  where:
  x << [1, 2]
  y << [1, 2]
}
'''

    then:
    SpockExecutionException see = thrown()
    see.message == /No argument was provided for parameters: 'z'/
  }

  @Issue("https://github.com/spockframework/spock/issues/652")
  @Unroll
  def "data variable that is not a parameter"() {
    expect:
    runner.runSpecBody """
def foo(x) {
  expect:
  x == y

  where:
  $parameterizations
}
    """

    where:
    parameterizations << [
        "x << [1,2]; y << [1, 2]",
        "[x, y] << [[1, 1], [2, 2]]",
        "x << [1, 2]; y = x"
    ]
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
    runner.runSpecBody """
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

  @Issue("https://github.com/spockframework/spock/issues/651")
  def "data values are injected by name, not order"(y, x) {
    expect:
    2 * x == y

    where:
    x << [1, 2]
    y << [2, 4]
  }

  @Issue("https://github.com/spockframework/spock/issues/652")
  def "method parameters that are eventually provided by extensions throw an exception at runtime if not set"() {
    when:
    runner.runSpecBody '''
def foo(x, y) {
  expect:
  x == y
}
  '''

    then:
    SpockExecutionException see = thrown()
    see.message == /No argument was provided for parameters: 'x', 'y'/
  }

  @Issue("https://github.com/spockframework/spock/issues/1279")
  def "data values are casted with type-coercion"(Integer x, int y) {
    expect:
    x.getClass() == Integer
    y.getClass() == Integer
    x == 10
    y in [20, 30]

    where:
    x = "10"
    y << ["20", "30"]
  }
}
