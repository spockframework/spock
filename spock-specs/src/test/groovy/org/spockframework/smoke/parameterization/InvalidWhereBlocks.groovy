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

  @Issue("http://issues.spockframework.org/detail?id=163")
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

  @Issue("http://issues.spockframework.org/detail?id=14")
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

  @Issue("http://issues.spockframework.org/detail?id=139")
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
    rhs                                 | _
    "[1, instanceField, 2]"             | _
    "{ -> { -> [instanceField] }() }()" | _
  }
}
