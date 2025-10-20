/*
 * Copyright 2008 the original author or authors.
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

import org.spockframework.runtime.GroovyRuntimeUtil
import org.spockframework.runtime.SpockComparisonFailure
import spock.lang.Issue
import spock.lang.Requires
import spock.lang.ResourceLock

import java.sql.Date

import static java.lang.Math.min
import static java.lang.Integer.MAX_VALUE
import static java.lang.Thread.State.BLOCKED

/**
 * Describes rendering of whole conditions.
 *
 * @author Peter Niederwieser
 */
class ConditionRendering extends ConditionRenderingSpec {
  def "simple condition"() {
    expect:
    isRendered """
x == 1
| |
2 false
    """, {
      def x = 2
      assert x == 1
    }
  }

  def "multi-line condition"() {
    expect:
    isRendered """
1 + 2 == 4 - 2
  |   |    |
  3   |    2
      false
    """, {
      assert 1 +
          2 ==



          4 -

          2

    }
  }

  private one(x) { 0 }

  def "MethodCallExpression with implicit target"() {
    expect:
    isRendered """
one(a)
|   |
0   1
    """, {
      def a = 1
      assert one(a)
    }
  }

  def "MethodCallExpression with explicit target"() {
    expect:
    isRendered """
a.get(b) == null
| |   |  |
| 1   0  false
[1]
    """, {
      def a = [1]
      def b = 0
      assert a.get(b) == null
    }
  }

  void "MethodCallExpression with GString method"() {
    expect:
    isRendered """
[1]."\$x"(0) == null
    | |     |
    1 get   false
    """, {
      def x = "get"
      assert [1]."$x"(0) == null
    }
  }

  def "MethodCallExpression invoking static method"() {
    expect:
    isRendered """
Math.max(a,b) == null
|    |   | |  |
|    2   1 2  false
class java.lang.Math
    """, {
      def a = 1
      def b = 2
      assert Math.max(a,b) == null
    }
  }

  def "MethodCallExpression with spread-dot operator"() {
    expect:
    isRendered """
["1", "22"]*.size() == null
             |      |
             [1, 2] false
    """, {
      assert ["1", "22"]*.size() == null
    }
  }

  def "MethodCallExpression with safe operator"() {
    expect:
    isRendered """
a?.foo()
|  |
|  null
null
    """, {
      def a = null
      assert a?.foo()
    }
  }

  // checks that ConditionRewriter.convertConditionNullAware() records values correctly
  def "top-level MethodCallExpression"() {
    expect:
    isRendered """
a.get(b)
| |   |
| 0   0
[0]
    """, {
      def a = [0]
      def b = 0
      assert a.get(b)
    }
  }

  def "MethodCallExpression with named arguments"() {
    expect:
    isRendered """
person.eat(what: steak, where: tokyo)
|      |         |             |
p      null      steak         tokyo
    """, {
      def person = new Person2()
      def steak = "steak"
      def tokyo = "tokyo"
      assert person.eat(what: steak, where: tokyo)
    }
  }

  def "MethodCallExpression with named arguments passed as map"() {
    expect:
    isRendered """
person.eat([what: steak, where: tokyo])
|      |          |             |
p      null       steak         tokyo
    """, {
      def person = new Person2()
      def steak = "steak"
      def tokyo = "tokyo"
      assert person.eat([what: steak, where: tokyo])
    }
  }

  def "StaticMethodCallExpression"() {
    expect:
    isRendered """
min(a,b) == null
|   | |  |
1   1 2  false
    """, {
      def a = 1
      def b = 2
      assert min(a,b) == null
    }
  }

  // checks that ConditionRewriter.convertConditionNullAware() records values correctly
  def "top-level StaticMethodCallExpression"() {
    expect:
    isRendered """
min(a,b)
|   | |
0   0 0
    """, {
      def a = 0
      def b = 0
      assert min(a,b)
    }
  }

  def "ConstructorCallExpression"() {
    expect:
    isRendered """
new ArrayList(a) == null
|             |  |
[]            1  false
    """, {
      def a = 1
      assert new ArrayList(a) == null
    }
  }

  def "ConstructorCallExpression for non-static inner class"() {
    when:
    runner.runSpecBody '''
      def test() {
        expect:
        new NonStaticInnerClass() == null
      }

      class NonStaticInnerClass {
        String toString() { "nsi" }
      }
    '''

    then:
    SpockComparisonFailure e = thrown()
    isRendered """
new NonStaticInnerClass() == null
|                         |
nsi                       false
    """, e.condition
  }

  def "ConstructorCallExpression with named arguments"() {
    expect:
    isRendered """
new Person2(name: fred, age: fredsAge) == null
|                 |          |         |
p                 fred       25        false
    """, {
      def fred = "fred"
      def fredsAge = 25
      assert new Person2(name: fred, age: fredsAge) == null
    }
  }

  def "ConstructorCallExpression with named arguments passed as map"() {
    expect:
    isRendered """
new Person2([name: fred, age: fredsAge]) == null
|                  |          |          |
p                  fred       25         false
    """, {
      def fred = "fred"
      def fredsAge = 25
      assert new Person2([name: fred, age: fredsAge]) == null
    }
  }

  def "TernaryExpression"() {
    expect:
    isRendered """
a ? b : c
|   |
1   0
    """, {
      def a = 1
      def b = 0
      def c = 1
      assert a ? b : c
    }

    isRendered """
a ? b : c
|       |
0       0
    """, {
      def a = 0
      def b = 1
      def c = 0
      assert a ? b : c
    }
  }

  def "ShortTernaryExpression"() {
    expect:
    isRendered """
(a ?: b) == null
 |       |
 1       false
    """, {
      def a = 1
      def b = 2
      assert (a ?: b) == null
    }

    isRendered """
a ?: b
|    |
0    0
    """, {
      def a = 0
      def b = 0
      assert a ?: b
    }
  }

  def "BinaryExpression"() {
    expect:
    isRendered """
a * b
| | |
0 0 1
    """, {
      def a = 0
      def b = 1
      assert a * b
    }

    isRendered """
a[b]
|||
||0
|false
[false]
    """, {
      def a = [false]
      def b = 0
      assert a[b]
    }
  }

  def "PrefixExpression"() {
    expect:
    isRendered """
++x == null
|   |
1   false
    """, {
      def x = 0
      assert ++x == null
    }
  }

  @Requires({ GroovyRuntimeUtil.MAJOR_VERSION == 2 })
  def "PostfixExpression (Groovy 2)"() {
    expect:
    isRendered """
x++ == null
 |  |
 0  false
    """, {
      def x = 0
      assert x++ == null
    }
  }

  @Requires({ GroovyRuntimeUtil.MAJOR_VERSION >= 3 })
  def "PostfixExpression"() {
    expect:
    isRendered """
x++ == null
|   |
0   false
    """, {
      def x = 0
      assert x++ == null
    }
  }

  def "BooleanExpression"() {
    expect:
    isRendered """
a
|
null
    """, {
      def a = null
      assert a
    }
  }

  def "ClosureExpression"() {
    expect:
    isRendered """
{ -> 1 + 2 } == null
             |
             false
    """, {
      assert { -> 1 + 2 } == null
    }
  }

  def "TupleExpression"() {
    // TupleExpression is only used on LHS of (multi-)assignment,
    // but LHS of assignment is not rewritten
    expect:
    isRendered """
((a,b) = [1,2]) && false
       |        |
       [1, 2]   false
    """, {
      def a
      def b
      assert ((a,b) = [1,2]) && false
    }
  }

  def "MapExpression"() {
    expect:
    isRendered """
[a:b, c:d] == null
   |    |  |
   2    4  false
    """, {
      def b = 2
      def d = 4
      assert [a:b, c:d] == null
    }

    isRendered """
[(a):b, (c):d] == null
 |   |  |   |  |
 1   2  3   4  false
    """, {
      def a = 1
      def b = 2
      def c = 3
      def d = 4
      assert [(a):b, (c):d] == null
    }
  }

  def "ListExpression"() {
    expect:
    isRendered """
[a,b,c] == null
 | | |  |
 1 2 3  false
    """, {
      def a = 1
      def b = 2
      def c = 3
      assert [a,b,c] == null
    }
  }

  def "RangeExpression"() {
    expect:
    isRendered """
(a..b) == null
 |  |  |
 1  2  false
    """, {
      def a = 1
      def b = 2
      assert (a..b) == null
    }

    isRendered """
(a..<b) == null
 |   |  |
 1   2  false
    """, {
      def a = 1
      def b = 2
      assert (a..<b) == null
    }
  }

  def "PropertyExpression"() {
    expect:
    isRendered """
a.empty == true
| |     |
| false false
[9]
    """, {
      def a = [9]
      assert a.empty == true
    }

    isRendered """
Integer.MIN_VALUE == null
|       |         |
|       |         false
|       -2147483648
class java.lang.Integer
    """, {
      assert Integer.MIN_VALUE == null
    }
  }

  def "AttributeExpression"() {
    expect:
    isRendered """
holder.@x
|       |
h       0
    """, {
      def holder = new Holder()
      assert holder.@x
    }
  }

  def "MethodPointerExpression"() {
    expect:
    isRendered """
a.&"\$b" == null
|    |  |
[]   |  false
     get
    """, {
      def a = []
      def b = "get"
      assert a.&"$b" == null
    }
  }

  def "ConstantExpression"() {
    expect:
    isRendered """
1 == "abc"
  |
  false
    """, {
      assert 1 == "abc"
    }
  }

  def "ClassExpression"() {
    expect:
    isRendered """
List == String
|    |  |
|    |  class java.lang.String
|    false
interface java.util.List
    """, {
      assert List == String
    }
  }

  @Requires({ GroovyRuntimeUtil.MAJOR_VERSION == 2 })  //comments are no longer included in power assertion's error message in Groovy 3
  def "ClassExpression with dot-containing comments (Groovy 2)"() {
    expect:
    isRendered """
java.util./*.awt.*/List == java.lang.String // I. Like. Dots.
                   |    |            |
                   |    false        class java.lang.String
                   interface java.util.List
    """, {
      assert java.util./*.awt.*/List == java.lang.String // I. Like. Dots.
    }
  }

  @Requires({ GroovyRuntimeUtil.MAJOR_VERSION >= 3 })
  def "ClassExpression with dot-containing comments"() {
    expect:
    isRendered """
java.util./*.awt.*/List == java.lang.String
                   |    |            |
                   |    false        class java.lang.String
                   interface java.util.List
    """, {
      assert java.util./*.awt.*/List == java.lang.String // I. Like. Dots.
    }
  }

  @ResourceLock("TimeZone.default")
  def "ClassExpression with different classes with same name"() {
    given:
    def defaultTimeZone = TimeZone.default
    TimeZone.default = TimeZone.getTimeZone('UTC')

    expect:
    isRendered """
date.getClass() == Date
|    |          |  |
|    |          |  class java.sql.Date
|    |          false
|    class java.util.Date
Thu Jan 01 00:00:00 UTC 1970
    """, {
      def date = new java.util.Date(0)
      assert date.getClass() == Date
    }

    cleanup:
    TimeZone.default = defaultTimeZone
  }

  @ResourceLock("TimeZone.default")
  def "instanceof expression"() {
    given:
    def defaultTimeZone = TimeZone.default
    TimeZone.default = TimeZone.getTimeZone('UTC')

    expect:
    isRendered """
date instanceof Date
|    |          |
|    false      class java.sql.Date
Thu Jan 01 00:00:00 UTC 1970 (java.util.Date)
    """, {
      def date = new java.util.Date(0)
      assert date instanceof Date
    }

    cleanup:
    TimeZone.default = defaultTimeZone
  }

  def "VariableExpression"() {
    expect:
    isRendered """
x
|
0
    """, {
      def x = 0
      assert x
    }
  }

  def "GStringExpression"() {
    expect:
    isRendered '''
"$a and ${b + c}" == null
  |       | | |   |
  1       2 5 3   false
    ''', {
      def a = 1
      def b = 2
      def c = 3
      assert "$a and ${b + c}" == null
    }
  }

  def "ArrayExpression"() {
    expect:
    isRendered """
new int[a][b] == null
        |  |  |
        1  2  false
    """, {
      def a = 1
      def b = 2
      assert new int[a][b] == null
    }
  }

  private two(a, b) { 0 }

  def "SpreadExpression"() {
    expect:
    isRendered """
two(*a)
|    |
0    [1, 2]
    """, {
      def a = [1, 2]
      assert two(*a)
    }

    isRendered """
[1, *a] == null
     |  |
     |  false
     [2, 3]
    """, {
      def a = [2, 3]
      assert [1, *a] == null
    }
  }

  def "SpreadMapExpression"() {
    expect:
    isRendered """
one(*:m)
|     |
0     [a:1, b:2]
    """, {
      def m = [a: 1, b: 2]
      assert one(*:m)
    }

    isRendered """
[a:1, *:m] == null
        |  |
        |  false
        [b:2, c:3]
    """, {
      def m = [b: 2, c: 3]
      assert [a:1, *:m] == null
    }
  }

  def "NotExpression"() {
    expect:
    isRendered """
!a
||
|true
false
    """, {
      def a = true
      assert !a
    }
  }

  def "UnaryMinusExpression"() {
    expect:
    isRendered """
-a == null
|| |
|1 false
-1
    """, {
      def a = 1
      assert -a == null
    }
  }

  def "UnaryPlusExpression"() {
    expect:
    isRendered """
+a == null
|| |
|1 false
1
    """, {
      def a = 1
      assert +a == null
    }
  }

  def "BitwiseNegationExpression"() {
    expect:
    isRendered """
~a == null
|| |
|1 false
-2
    """, {
      def a = 1
      assert ~a == null
    }
  }

  def "CastExpression"() {
    expect:
    isRendered """
(List)a
      |
      null
    """, {
      def a = null
      assert (List)a
    }

    isRendered """
a as int[]
|
null
    """, {
      def a = null
      assert a as int[]
    }
  }

  private three(a, b, c) { 0 }

  def "ArgumentListExpression"() {
    expect:
    isRendered """
three(a, b,c)
|     |  | |
0     1  2 3
    """, {
      def a = 1
      def b = 2
      def c = 3
      assert three(a, b,c)
    }
  }

  // as of Groovy 1.7.3, represented as FieldExpression
  @Issue("https://github.com/spockframework/spock/issues/228")
  def "statically imported field"() {
    expect:
    isRendered """
MAX_VALUE == 0
|         |
|         false
2147483647
    """, {
      assert MAX_VALUE == 0
    }
  }

  // as of Groovy 1.7.3, represented as PropertyExpression
  def "statically imported enum value"() {
    expect:
    isRendered """
BLOCKED == 0
        |
        false
    """, {
      assert BLOCKED == 0
    }
  }

  // for implicit closure calls see ImplicitClosureCallRendering
  void "explicit closure call"() {
    def func = { it }
    expect:
    isRendered """
func.call(42) == null
|    |        |
|    42       false
${func.dump()}
    """, {
      assert func.call(42) == null
    }
  }

  def "properly escape backslashes in rendering"() {
    expect:
    isRendered """
":\\":\\\\" == null
         |
         false
    """, {
      assert ":\":\\" == null
    }
  }

  def "nested condition does not disturb rendering of outer condition"() {
    expect:
    isRendered """
!list.every { assert 9 != 8; true }
||    |
||    true
|[1, 2, 3]
false
        """, {
      def list = [1, 2, 3]
      assert !list.every { assert 9 != 8; true }
    }
  }

  /*
  def "MapEntryExpression"() {
      // tested as part of testMapExpression
  }

  def "DeclarationExpression"() {
      // cannot occur in condition
  }

  def "ClosureListExpression"() {
      // cannot occur in condition
  }

  def "BytecodeExpression"() {
      // cannot occur in condition
  }
  */

  static class Holder {
    public x = 0

    def getX() { 9 }

    String toString() {"h"}
  }

  static class Person2 {
    def name
    def age
    def height

    def eat(args) { null }

    String toString() { "p" }
  }
}
