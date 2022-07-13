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

package org.spockframework.smoke.parameterization

import org.spockframework.EmbeddedSpecification
import org.spockframework.compiler.InvalidSpecCompileException
import spock.lang.Issue

/**
 * @author Peter Niederwieser
 */
class InvalidWhereBlocks extends EmbeddedSpecification {
  def "data variable collides with local variable"() {
    when:
    compiler.compileFeatureBody """
def x = 0

expect:
x == y

where:
x << [1, 2]
y << [1, 2]
    """

    then:
    thrown(InvalidSpecCompileException)
  }

  def "data variable hides (but doesn't collide with) instance field"() {
    when:
    compiler.compileSpecBody """
def x = 1

def foo() {
  expect:
  x == 2

  where:
  x << 2
}
    """

    then:
    noExceptionThrown()
  }

  def "data variable hides (but doesn't collide with) static field"() {
    when:
    compiler.compileSpecBody """
static String x = 1

def foo() {
  expect:
  x == 2

  where:
  x << 2
}
    """

    then:
    noExceptionThrown()
  }

  def "duplicate data variable"() {
    when:
    compiler.compileFeatureBody """
expect:
true

where:
x << 1
x << 2
    """

    then:
    thrown(InvalidSpecCompileException)
  }

  @Issue("https://github.com/spockframework/spock/issues/285")
  def "duplicate explicit data variable"() {
    when:
    compiler.compileSpecBody """
def foo(x) {
  expect:
  true

  where:
  x << 1
  x << 2
}
    """

    then:
    thrown(InvalidSpecCompileException)
  }

  @Issue("https://github.com/spockframework/spock/issues/137")
  def "duplicate data variable in multi-parameterization"() {
    when:
    compiler.compileFeatureBody """
expect:
true

where:
[x, x] << [[1, 1]]
    """

    then:
    thrown(InvalidSpecCompileException)
  }

  @Issue("https://github.com/spockframework/spock/issues/261")
  def "right-hand side of parameterization may not reference instance fields (only @Shared and static fields)"() {
    when:
    compiler.compileSpecBody """
def instanceField

def foo() {
  expect:
  x == 1

  where:
  x << $rhs
}
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.message.contains("@Shared")

    where:
    rhs |_
    "[1, instanceField, 2]" |_
    "{ -> { -> [instanceField] }() }()" |_
  }

  def "right-hand side of data variable may not reference instance fields (only @Shared and static fields)"() {
    when:
    compiler.compileSpecBody """
def instanceField

def foo() {
  expect:
  x == 1

  where:
  x = $rhs
}
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.message.contains("@Shared")

    where:
    rhs |_
    "instanceField" |_
    "{ -> { -> instanceField }() }()" |_
  }

  def 'referencing a data variable in a data provider is not allowed'() {
    when:
    runner.runFeatureBody """
expect:
true

where:
x << [1, 2].iterator()
y << x
"""

    then:
    thrown(MissingPropertyException)
  }

  def 'referencing a data variable in a closure in a data provider is not allowed'() {
    when:
    runner.runFeatureBody """
expect:
true

where:
x << [1, 2].iterator()
y << { x }()
"""

    then:
    thrown(MissingPropertyException)
  }

  def 'referencing a data table variable in a data table like data provider is not allowed'() {
    when:
    runner.runFeatureBody """
expect:
true

where:
a | b
1 | 2
3 | 4

c << [a, 5]
"""

    then:
    thrown(MissingPropertyException)
  }

  def 'referencing a data table parameter in a data table cell is not allowed'() {
    when:
    compiler.compileFeatureBody '''
expect:
true

where:
a | b
1 | $spock_p_a
'''

    then:
    thrown(InvalidSpecCompileException)
  }

  def 'referencing a data table parameter in a closure in a data table cell is not allowed'() {
    when:
    compiler.compileFeatureBody '''
expect:
true

where:
a | b
1 | { $spock_p_a }()
'''

    then:
    thrown(InvalidSpecCompileException)
  }

  def 'referencing a data table parameter in a data provider is not allowed'() {
    when:
    runner.runFeatureBody '''
expect:
true

where:
a | _
1 | _

c << $spock_p_a
'''

    then:
    thrown(MissingPropertyException)
  }
}
