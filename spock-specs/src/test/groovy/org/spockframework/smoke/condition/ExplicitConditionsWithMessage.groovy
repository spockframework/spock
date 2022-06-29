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

package org.spockframework.smoke.condition

import org.spockframework.runtime.ConditionNotSatisfiedError

import spock.lang.FailsWith
import spock.lang.Issue

/**
 * @author Peter Niederwieser
 */
@Issue(["https://github.com/spockframework/spock/issues/145", "https://github.com/spockframework/spock/issues/922"])
class ExplicitConditionsWithMessage extends ConditionRenderingSpec {
  def "evaluation of satisfied condition"() {
    expect:
    assert 1 + 2 == 3, "need to brush up my math"
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "evaluation of unsatisfied condition"() {
    expect:
    assert 1 + 2 == 2, "need to brush up my math"
  }

  def "rendering of simple message"() {
    expect:
    isRendered """
1 + 2 == 2
  |   |
  3   false

need to brush up my math
    """, {
      assert 1 + 2 == 2, "need to brush up my math"
    }
  }

  def "rendering of GString message"() {
    expect:
    isRendered """
a + b == 2
| | | |
1 3 2 false

a: 1 b: 2
    """, {
      def a = 1
      def b = 2
      assert a + b == 2, "a: $a b: ${b * 2 / 2}"
    }
  }

  def "rendering of object message"() {
    expect:
    isRendered """
map.a + map.b == 2
|   | | |   | |
|   1 3 |   2 false
|       [a:1, b:2]
[a:1, b:2]

[a:1, b:2]
    """, {
      def map = [a: 1, b: 2]
      assert map.a + map.b == 2, map
    }
  }

  // null message and no message have same representation in AST
  def "rendering of null message"() {
    expect:
    isRendered """
1 + 2 == 2
  |   |
  3   false
    """, {
      assert 1 + 2 == 2, null
    }
  }

  // null message and no message currently have same representation (null) in SpockRuntime
  def "rendering of indirect null message"() {
    expect:
    isRendered """
1 + 2 == 2
  |   |
  3   false
    """, {
      def x = null
      assert 1 + 2 == 2, x
    }
  }

  void "explicit assertion with message"() {
    expect:
    isRendered """
a == b
| |  |
1 |  2
  false

Additional message
    """, {
      def a = 1
      def b = 2
      assert a == b : "Additional message"
    }
  }

  void "explicit assertion with methodCondition and message"() {
    expect:
    isRendered """
a.contains(b)
| |        |
| false    bar
foo

Additional message
    """, {
      def a = 'foo'
      def b = 'bar'
      assert a.contains(b) : "Additional message"
    }
  }


  void "explicit assertion with static methodCondition and message"() {
    expect:
    isRendered """
Boolean.parseBoolean(a)
|       |            |
|       false        foo
class java.lang.Boolean

Additional message
    """, {
      def a = 'foo'
      assert Boolean.parseBoolean(a) : "Additional message"
    }
  }

}
