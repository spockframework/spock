/*
 * Copyright 2012 the original author or authors.
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

import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.GroovyRuntimeUtil
import spock.lang.Issue
import spock.lang.Requires

class EqualityComparisonRendering extends ConditionRenderingSpec {
  def "values with different representations"() {
    expect:
    isRendered """
x == y
| |  |
1 |  2
  false
    """, {
      def x = 1
      def y = 2
      assert x == y
    }
  }

  def "values with same representations and same types"() {
    given:
    def x = new Person(name: "Fred")
    def y = new Person(name: "Fred")

    when:
    assert x == y

    then:
    ConditionNotSatisfiedError e = thrown()
    def rendering = e.condition.rendering.trim().replaceAll(/(?<=@)\p{XDigit}++/, 'X')
    def expectedRendering = '''
x == y
| |  |
| |  Fred (org.spockframework.smoke.condition.EqualityComparisonRendering.Person@X)
| false
Fred (org.spockframework.smoke.condition.EqualityComparisonRendering.Person@X)
    '''.trim()
    rendering == expectedRendering
  }

  def "values with same representations and different types"() {
    expect:
    isRendered """
x == y
| |  |
| |  1 (java.lang.String)
| false
1 (java.lang.Integer)
    """, {
      def x = 1
      def y = "1"
      assert x == y
    }
  }

  def "values with same representations and different array types"() {
    expect:
    isRendered """
x == y
| |  |
| |  [1] (java.lang.String[])
| false
[1] (int[])
    """, {
      def x = [1] as int[]
      def y = ['1'] as String[]
      assert x == y
    }
  }

  def "values with same representations and different anonymous types"() {
    expect:
    isRendered '''
x == y
| |  |
| |  foo (org.spockframework.smoke.condition.EqualityComparisonRendering$2)
| false
foo (org.spockframework.smoke.condition.EqualityComparisonRendering$1)
    ''', {
      def x = new Serializable() { String toString() { "foo" } }
      def y = new Serializable() { String toString() { "foo" } }
      assert x == y
    }
  }

  def "values with same rendered and literal representations"() {
    expect:
    isRendered """
x == 123
| |  |
| |  123 (java.lang.Integer)
| false
123 (java.lang.String)
    """, {
      def x = "123"
      assert x == 123
    }
  }

  @Requires({ GroovyRuntimeUtil.isGroovy2() })
  def "values with same literal representations (Groovy 2)"() {
    expect:
    isRendered """
[0, 1] == [0, 1] as Set
|      |         |
|      false     [0, 1] (java.util.LinkedHashSet)
[0, 1] (java.util.ArrayList)
    """, {
      assert [0, 1] == [0, 1] as Set
    }
  }

  @Requires({ GroovyRuntimeUtil.isGroovy3orNewer() })
  def "values with same literal representations"() {
    expect:
    isRendered """
[0, 1] == [0, 1] as Set
|      |  |
|      |  [0, 1] (java.util.LinkedHashSet)
|      false
[0, 1] (java.util.ArrayList)
    """, {
      assert [0, 1] == [0, 1] as Set
    }
  }

  def "null values"() {
    expect:
    isRendered """
x == y
| |  |
| |  null (java.lang.String)
| false
null (void)
    """, {
      def x = null
      def y = "null"
      assert x == y
    }
  }

  def "type hints are also added for nested equality comparisons"() {
    expect:
    isRendered """
(x == y) instanceof String
 | |  |  |          |
 | |  |  false      class java.lang.String
 | |  1 (java.lang.String)
 | false (java.lang.Boolean)
 1 (java.lang.Integer)
    """, {
      def x = 1
      def y = "1"
      assert (x == y) instanceof String
    }
  }

  @Issue("https://github.com/spockframework/spock/issues/374")
  def "type hints are not added for nested equality comparisons if values are equal"() {
    expect:
    isRendered """
(x == y) instanceof String
 | |  |  |          |
 1 |  1  false      class java.lang.String
   true (java.lang.Boolean)
    """, {
      int x = 1
      BigDecimal y = 1
      assert (x == y) instanceof String
    }
  }

  def "type hints are not added when equals method is used (but only when equality operator is used)"() {
    expect:
    isRendered """
x.equals(y)
| |      |
1 false  1
    """, {
      def x = 1
      def y = "1"
      assert x.equals(y)
    }
  }

  static class Person {
    String name
    String toString() { name }
    boolean equals(other) { false }
  }
}

