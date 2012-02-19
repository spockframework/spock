/*
 * Copyright 2012 the original author or authors.
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

  def "values with same representations and same types"() {
    expect:
    isRendered """
x == y
| |  |
| |  Fred (org.spockframework.smoke.condition.Person3)
| false
Fred (org.spockframework.smoke.condition.Person3)
    """, {
      def x = new Person3(name: "Fred")
      def y = new Person3(name: "Fred")
      assert x == y
    }
  }

  def "values with same rendered and literal representations"() {
    expect:
    isRendered """
x == 123
| |
| false
123 (java.lang.String)
    """, {
      def x = "123"
      assert x == 123
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

  def "type hints are also added for nested expressions"() {
    expect:
    isRendered """
(x == y) instanceof String
 | |  |  |
 | |  |  false
 | |  1 (java.lang.String)
 | false
 1 (java.lang.Integer)
    """, {
      def x = 1
      def y = "1"
      assert (x == y) instanceof String
    }
  }

  def "no type hints are added for equals method"() {
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
}

private class Person3 {
  String name
  String toString() { name }
  boolean equals(other) { false }
}
