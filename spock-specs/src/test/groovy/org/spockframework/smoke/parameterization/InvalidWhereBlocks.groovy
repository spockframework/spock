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
import spock.lang.PendingFeature

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

  def 'using cross_product label outside where block is not allowed'() {
    when:
    def result = compiler.transpileFeatureBody '''
      cross_product:
      true

      expect:
      true

      where:
      a | _
      1 | _
    '''

    then:
    result.source.normalize() == '''
      |Unable to produce AST for this phase due to earlier compilation error:
      |startup failed:
      |script.groovy: 2: Unrecognized block label: cross_product @ line 2, column 7.
      |         true
      |         ^
      |
      |1 error
    '''.stripMargin().trim()
  }

  def 'using cross_product label between data table and something else is not allowed'() {
    when:
    def result = compiler.transpileFeatureBody '''
      expect:
      true

      where:
      a | _
      1 | _
      cross_product:
      b = 1
    '''

    then:
    result.source.normalize() == '''
      |Unable to produce AST for this phase due to earlier compilation error:
      |startup failed:
      |script.groovy: 8: Cross product label must only appear between two data tables with the same type of separator @ line 8, column 7.
      |         b = 1
      |         ^
      |
      |1 error
    '''.stripMargin().trim()
  }

  @PendingFeature(reason = "not implemented yet and not planned, but if someone does, he will notice and can update the docs")
  def 'using cross_product label between data tables with different separator type is not yet allowed'() {
    when:
    def result = compiler.transpileFeatureBody '''
      expect:
      true

      where:
      a | _
      1 | _
      cross_product:
      b ; _
      1 ; _
      cross_product:
      c | _
      1 | _
    '''

    then:
    !result.source.contains('Cross product label must only appear between two data tables')
  }

  def 'using cross_product label within semicolon data table row is not yet allowed'() {
    when:
    def result = compiler.transpileFeatureBody '''
      expect:
      true

      where:
      a ; cross_product: b
      1 ; 2
    '''

    then:
    result.source.normalize() == '''
      |Unable to produce AST for this phase due to earlier compilation error:
      |startup failed:
      |script.groovy: 5: Cross product label must only appear between two data tables with the same type of separator @ line 5, column 26.
      |         a ; cross_product: b
      |                            ^
      |
      |script.groovy: 6: Data table must have more than just the header row @ line 6, column 7.
      |         1 ; 2
      |         ^
      |
      |2 errors
    '''.stripMargin().trim()
  }
}
