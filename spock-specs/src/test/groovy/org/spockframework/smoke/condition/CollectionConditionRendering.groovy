/*
 * Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.smoke.condition

import spock.lang.Issue

class CollectionConditionRendering extends ConditionRenderingSpec {
  def "nested lenient matching"() {
    expect:
    isRendered """
!(x =~ y)
| | |  |
| | |  [1]
| | true
| [1]
false
    """, {
      def x = [1]
      def y = [1]
      assert !(x =~ y)
    }
  }

  @Issue("https://github.com/spockframework/spock/issues/1930")
  def "nested regex finding"() {
    expect:
    isRendered """
(x =~ y).count == 0
 | |  |  |     |
 | |  .  3     false
 | java.util.regex.Matcher[pattern=. region=0,3 lastmatch=]
 foo
    """, {
      def x = 'foo'
      def y = /./
      assert (x =~ y).count == 0
    }
  }

  @Issue("https://github.com/spockframework/spock/issues/1930")
  def "nested regex complex finding"() {
    expect:
    isRendered """
(output =~ /on (executor-\\d+)/).collect { it[1] }.unique().size() == 3
 |      |                       |                 |        |      |
 |      |                       |                 |        2      false
 |      |                       |                 [executor-1, executor-2]
 |      |                       [executor-1, executor-2]
 |      java.util.regex.Matcher[pattern=on (executor-\\d+) region=0,54 lastmatch=]
 Foo on executor-1
 Bar on executor-2
 Baz on executor-1""", {
      def output = '''\
Foo on executor-1
Bar on executor-2
Baz on executor-1
'''
      assert (output =~ /on (executor-\d+)/).collect { it[1] }.unique().size() == 3
    }
  }

  def "rendering of lenient matching with variables"() {
    expect:
    isRendered """
x =~ y
| |  |
| |  [2]
| false
| 2 differences (0% similarity, 1 missing, 1 extra)
| missing: [2]
| extra: [1]
[1]
    """, {
      def x = [1]
      def y = [2]
      assert x =~ y
    }
  }

  def 'rendering of lenient matching with variable-literal'() {
    expect:
    isRendered """
x =~ [2, 2, 1, 3, 3]
| |
| false
| 2 differences (66% similarity, 1 missing, 1 extra)
| missing: [3]
| extra: [4]
[4, 1, 2]
    """, {
      def x = [4, 1, 2]
      assert x =~ [2, 2, 1, 3, 3]
    }
  }
  def 'rendering of lenient matching with literal-variable'() {
    expect:
    isRendered """
[4, 1, 2] =~ y
          |  |
          |  [2, 1, 3]
          false
          2 differences (66% similarity, 1 missing, 1 extra)
          missing: [3]
          extra: [4]
    """, {
      def y = [2, 2, 1, 3, 3]
      assert [4, 1, 2] =~ y
    }
  }

  def 'rendering of lenient matching with literals'() {
    expect:
    isRendered """
[4, 1, 2] =~ [2, 2, 1, 3, 3]
          |
          false
          2 differences (66% similarity, 1 missing, 1 extra)
          missing: [3]
          extra: [4]
    """, {
      assert [4, 1, 2] =~ [2, 2, 1, 3, 3]
    }
  }

  def "rendering of strict matching"() {
    expect:
    isRendered """
x ==~ y
| |   |
| |   [2]
| false
[1]

Expected: iterable with items [<2>] in any order
     but: not matched: <1>
    """, {
      def x = [1]
      def y = [2]
      assert x ==~ y
    }
  }

  def "rendering of nested strict matching"() {
    expect:
    isRendered """
!(x ==~ y)
| | |   |
| | |   [1]
| | true
| [1]
false
    """, {
      def x = [1]
      def y = [1]
      assert !(x ==~ y)
    }
  }

  def "nested regex matching"() {
    expect:
    isRendered """
!(x ==~ y)
| | |   |
| a |   .
|   true
false
    """, {
      def x = 'a'
      def y = /./
      assert !(x ==~ y)
    }
  }

  def "indirect regex find works with different representations"() {
    expect:
    isRendered """
x =~ y
| |  |
| |  \\d
| java.util.regex.Matcher[pattern=\\d region=0,3 lastmatch=]
[a]
    """, {
      def x = "[a]"
      def y = /\d/
      assert x =~ y
    }
  }

  def "regex find works with different representations"() {
    expect:
    isRendered """
x =~ /\\d/
| |
| java.util.regex.Matcher[pattern=\\d region=0,3 lastmatch=]
[a]
    """, {
      def x = "[a]"
      assert x =~ /\d/
    }
  }

  def "regex find doesn't choke on non-String types"() {
    expect:
    isRendered """
x =~ y
| |  |
| |  <java.lang.Object@ed7f8b4>
| java.util.regex.Matcher[pattern=<java.lang.Object@ed7f8b4> region=0,3 lastmatch=]
[a]
    """, {
      def x = "[a]"
      def y = new Object() {
        @Override
        String toString() {
          // just fake the normal toString() here to get constant ids
          return "<java.lang.Object@ed7f8b4>"
        }
      }
      assert x =~ y
    }
  }

  def "indirect regex match works with different representations"() {
    expect:
    isRendered """
x ==~ y
| |   |
| |   a
| false
[a]
    """, {
      def x = "[a]"
      def y = /a/
      assert x ==~ y
    }
  }

  def "regex match with different representations"() {
    expect:
    isRendered """
x ==~ /a/
| |
| false
[a]
    """, {
      def x = "[a]"
      assert x ==~ /a/
    }
  }

  def "regex match doesn't choke on non-String types"() {
    expect:
    isRendered """
x ==~ y
| |   |
| |   <java.lang.Object@ed7f8b4>
| false
[a]
    """, {
      def x = "[a]"
      def y = new Object() {
        @Override
        String toString() {
          // just fake the normal toString() here to get constant ids
          return "<java.lang.Object@ed7f8b4>"
        }
      }
      assert x ==~ y
    }
  }
}
