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
import spock.util.EmbeddedSpecification
import org.spockframework.util.SpockSyntaxException
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
    Exception e = thrown()
    containsSyntaxException(e)
  }

  def "data variable collides with instance field"() {
    when:
    compiler.compileSpeckBody """
def instanceField

def foo() {
  expect:
  true

  where:
  instanceField << 1
}
    """

    then:
    Exception e = thrown()
    containsSyntaxException(e)
  }

  def "data variable collides with static field"() {
    when:
    compiler.compileSpeckBody """
static String staticField = "foo"

def foo() {
  expect:
  true

  where:
  staticField << 1
}
    """

    then:
    Exception e = thrown()
    containsSyntaxException(e)
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
    Exception e = thrown()
    containsSyntaxException(e)
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
    Exception e = thrown()
    containsSyntaxException(e)
  }

  void containsSyntaxException(e) {
    assert e instanceof MultipleCompilationErrorsException
    assert e.errorCollector.errorCount == 1
    assert e.errorCollector.errors[0].cause instanceof SpockSyntaxException
  }
}