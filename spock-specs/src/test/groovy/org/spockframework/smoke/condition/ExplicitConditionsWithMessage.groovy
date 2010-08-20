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

package org.spockframework.smoke.condition

import org.spockframework.runtime.ConditionNotSatisfiedError
import spock.lang.FailsWith
import spock.lang.Issue

/**
 * @author Peter Niederwieser
 */
@Issue("http://issues.spockframework.org/detail?id=22")
class ExplicitConditionsWithMessage extends ConditionSpec {
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

need to brush up my math
    """, {
      assert 1 + 2 == 2, "need to brush up my math"
    }
  }

  def "rendering of GString message"() {
    expect:
    isRendered """
a + b == 2

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

  def "rendering of indirect null message"() {
    expect:
    isRendered """
1 + 2 == 2

null
    """, {
      def x = null
      assert 1 + 2 == 2, x
    }
  }
}