/*
 * Copyright 2026 the original author or authors.
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

import groovy.transform.CompileStatic

/**
 * Describes rendering of conditions in statically compiled code.
 * The rendered output must match the one of dynamic code, see {@link ConditionRendering}.
 */
@CompileStatic
class CompileStaticConditionRendering extends ConditionRenderingSpec {
  def "simple condition"() {
    expect:
    isRendered """
x == 1
| |
2 false
    """, {
      int x = 2
      assert x == 1
    }
  }

  private int one(int x) { 0 }

  def "MethodCallExpression with implicit target"() {
    expect:
    isRendered """
one(a)
|   |
0   1
    """, {
      int a = 1
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
      List<Integer> a = [1]
      int b = 0
      assert a.get(b) == null
    }
  }

  def "MethodCallExpression invoking static method"() {
    expect:
    // unlike in dynamic code, the target class of a static method call is not
    // rendered, because statically compiled code never evaluates the receiver
    // expression of a static method call, so its value is never recorded
    isRendered """
Math.max(a,b) == c
     |   | |  |  |
     2   1 2  |  0
              false
    """, {
      int a = 1
      int b = 2
      int c = 0
      assert Math.max(a,b) == c
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
a?.length()
|  |
|  null
null
    """, {
      String a = null
      assert a?.length()
    }
  }

  def "top-level MethodCallExpression"() {
    expect:
    isRendered """
a.contains(b)
| |        |
| false    2
[0]
    """, {
      List<Integer> a = [0]
      int b = 2
      assert a.contains(b)
    }
  }

  def "chained MethodCallExpressions"() {
    expect:
    isRendered """
a.first().toUpperCase() == "B"
| |       |             |
| a       A             false
[a]                     1 difference (0% similarity)
                        (A)
                        (B)
    """, {
      List<String> a = ["a"]
      assert a.first().toUpperCase() == "B"
    }
  }

  def "TernaryExpression"() {
    expect:
    isRendered """
a ? b : c
|   |
1   0
    """, {
      int a = 1
      int b = 0
      int c = 1
      assert a ? b : c
    }
  }

  def "BinaryExpression"() {
    expect:
    isRendered """
a * b
| | |
0 0 1
    """, {
      int a = 0
      int b = 1
      assert a * b
    }

    isRendered """
a[b]
|||
||0
|false
[false]
    """, {
      List<Boolean> a = [false]
      int b = 0
      assert a[b]
    }
  }

  def "comparison operators"() {
    expect:
    isRendered """
x > 43
| |
| false
42
    """, {
      int x = 42
      assert x > 43
    }
  }

  def "BooleanExpression"() {
    expect:
    isRendered """
a
|
null
    """, {
      String a = null
      assert a
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
      List<Integer> a = [9]
      assert a.empty == true
    }
  }

  def "instanceof expression"() {
    expect:
    isRendered """
x instanceof Integer
| |          |
| false      class java.lang.Integer
foo (java.lang.String)
    """, {
      Object x = "foo"
      assert x instanceof Integer
    }
  }

  def "VariableExpression"() {
    expect:
    isRendered """
x
|
0
    """, {
      Integer x = 0
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
      int a = 1
      int b = 2
      int c = 3
      assert "$a and ${b + c}" == null
    }
  }

  def "condition in with block"() {
    expect:
    isRendered """
size() == 3
|      |
2      false
    """, {
      List<String> list = ["a", "b"]
      with(list) {
        assert size() == 3
      }
    }
  }
}
