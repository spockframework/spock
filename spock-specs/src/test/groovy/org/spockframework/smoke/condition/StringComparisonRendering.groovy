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

import org.spockframework.runtime.ExpressionInfoValueRenderer
import org.spockframework.runtime.FailedStringComparisonRenderer
import spock.lang.Issue

class StringComparisonRendering extends ConditionRenderingSpec {
  def "shows differences between strings"() {
    expect:
    isRendered """
"the quick" == "the quirk"
            |
            false
            1 difference (88% similarity)
            the qui(c)k
            the qui(r)k
    """, {
      assert "the quick" == "the quirk"
    }
  }

  def "handles case where one side is null"() {
    expect:
    isRendered """
"foo" == null
      |
      false
    """, {
      assert "foo" == null
    }

    isRendered """
null == "foo"
     |
     false
    """, {
      assert null == "foo"
    }
  }

  def "shows differences between strings in subexpression"() {
    expect:
    isRendered """
("the quick" == "the quirk") instanceof String
             |               |          |
             |               false      class java.lang.String
             false
             1 difference (88% similarity)
             the qui(c)k
             the qui(r)k (java.lang.Boolean)
    """, {
      assert ("the quick" == "the quirk") instanceof String
    }
  }

  @Issue("https://github.com/spockframework/spock/issues/374")
  def "does not show differences if strings in subexpression are equal"() {
    expect:
    isRendered """
("the quick" == "the quick") instanceof String
             |               |          |
             |               false      class java.lang.String
             true (java.lang.Boolean)
    """, {
      assert ("the quick" == "the quick") instanceof String
    }
  }
  @Issue("https://github.com/spockframework/spock/issues/121")
  def "large String comparison does not cause OOM-Error, difference at start"() {
    StringBuilder sb = largeStringBuilder()
    String a = sb.toString()
    sb.setCharAt(0, 'b'  as char)
    String b = sb.toString()

    expect:
    renderedConditionContains({
      assert a == b
    },
      "1 difference (90% similarity) (comparing subset start: 0, end1: 11, end2: 11)",
      "(a)aaaaaaaaaa",
      "(b)aaaaaaaaaa"
    )
  }

  @Issue("https://github.com/spockframework/spock/issues/121")
  def "large String comparison does not cause OOM-Error, difference in the middle"() {
    StringBuilder sb = largeStringBuilder()
    String a = sb.toString()
    sb.setCharAt(sb.length() / 2 as int, 'b'  as char)
    String b = sb.toString()

    expect:
    renderedConditionContains({
      assert a == b
    },
      "1 difference (95% similarity) (comparing subset start: 12789, end1: 12811, end2: 12811)",
      "aaaaaaaaaaa(a)aaaaaaaaaa",
      "aaaaaaaaaaa(b)aaaaaaaaaa"
    )
  }

  @Issue("https://github.com/spockframework/spock/issues/121")
  def "large String comparison does not cause OOM-Error, difference at end"() {
    StringBuilder sb = largeStringBuilder()
    String a = sb.toString()
    sb.setCharAt(sb.length()-1, 'b'  as char)
    String b = sb.toString()

    expect:
    renderedConditionContains({
      assert a == b
    },
      "1 difference (91% similarity) (comparing subset start: 25588, end1: 25600, end2: 25600)",
      "aaaaaaaaaaa(a)",
      "aaaaaaaaaaa(b)")
  }



  @Issue("https://github.com/spockframework/spock/issues/121")
  def "large String comparison does not cause OOM-Error, difference at start and  end"() {
    StringBuilder sb = largeStringBuilder()
    String a = sb.toString()
    sb.setCharAt(0, 'b'  as char)
    sb.setCharAt(sb.length()-1, 'b'  as char)
    String b = sb.toString()

    expect:
    renderedConditionContains({
      assert a == b
    },
      "false",
      "Strings too large to calculate edit distance.")
  }


  @Issue("https://github.com/spockframework/spock/issues/121")
  def "large String comparison does not cause OOM-Error, complete difference"() {
    String a = largeStringBuilder()
    String b = largeStringBuilder("bbbbbbbbbbbbbbbb")


    expect:
    renderedConditionContains({
      assert a == b
    },
      "false",
      "Strings too large to calculate edit distance.")
  }


  def "large String comparison without room for context"() {
    int stringLength = Math.sqrt(FailedStringComparisonRenderer.MAX_EDIT_DISTANCE_MEMORY)

    String common = largeStringBuilder("cccccccccccccccc", stringLength)
    String a = largeStringBuilder("aaaaaaaaaaaaaaaa", stringLength) + common
    String b = largeStringBuilder("bbbbbbbbbbbbbbbb", stringLength) + common


    expect:
    renderedConditionContains({
      assert a == b
    },
      "false",
      a,
      b,
      "$stringLength differences (0% similarity) (comparing subset start: 0, end1: $stringLength, end2: $stringLength)")
  }


  def "String diff does not cause int overflow when shortening, causing OOM"() {
    int stringLength = Math.sqrt(Integer.MAX_VALUE) * 2

    String a = largeStringBuilder("aaaaaaaaaaaaaaaa", stringLength)
    String b = largeStringBuilder("bbbbbbbbbbbbbbbb", stringLength)


    expect:
    renderedConditionContains({
      assert a == b
    },
      "false",
      "Strings too large to calculate edit distance.")
  }


  @Issue("https://github.com/spockframework/spock/issues/737")
  def 'shows differences between string literals with line breaks'() {
    expect:
    isRendered '''
"""foo """ == """bar """
           |
           false
           4 differences (20% similarity)
           (foo)\\n(-~)
           (bar)\\n(\\n)
''', {
      assert """foo
""" == """bar

"""
    }
  }

  @Issue("https://github.com/spockframework/spock/issues/737")
  def 'shows differences between string literals with newline escapes'() {
    expect:
    isRendered '''
"""foo  """ == """bar  """
            |
            false
            3 differences (40% similarity)
            (foo)
            (bar)
''', {
      assert """\
foo  \
""" == """\
bar  \
"""
    }
  }

  @Issue("https://github.com/spockframework/spock/issues/737")
  def 'shows differences between string literals with line breaks and newline escapes'() {
    expect:
    isRendered '''
"""\\\\ foo""" == """\\\\ foo """
             |
             false
             8 differences (38% similarity)
             \\\\\\nfoo(--------~)
             \\\\\\nfoo(       \\n)
''', {
      assert """\\
foo\
""" == """\\
foo
"""
    }
  }

  @Issue("https://github.com/spockframework/spock/issues/737")
  def 'shows differences between interpolated string literals with line breaks and newline escapes'() {
    given:
    def a = 'foo'
    def b = 'bar'

    expect:
    isRendered '''
"""$a """ == """\\\\$b """
    |     |        |
    foo   |        bar
          false
          5 differences (16% similarity)
          (f~oo--)\\n
          (\\\\bar )\\n
''', {
      assert """\
$a\

""" == """\\\
$b
"""
    }
  }

  @Issue("https://github.com/spockframework/spock/issues/737")
  def 'shows differences between long string literals with line breaks and newline escapes'() {
    expect:
    isRendered '''
"""Lorem ipsum Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.""" == """Lorem ipsum Lorem ipsum  dolor sit amet,  consetetur sadipscing elitr, sed  diam nonumy eirmod tempor  invidunt ut labore et  dolore magna aliquyam erat, sed diam voluptua. """
                                                                                                                                                                              |
                                                                                                                                                                              false
                                                                                                                                                                              7 differences (95% similarity)
                                                                                                                                                                              Lorem ipsum\\n(\\n)Lorem ipsum (-)dolor sit amet, (-)consetetur sadipscing elitr, sed (-)diam nonumy eirmod tempor (-)invidunt ut labore et (-)dolore magna aliquyam erat, sed diam voluptua.(-~)
                                                                                                                                                                              Lorem ipsum\\n(-~)Lorem ipsum ( )dolor sit amet, ( )consetetur sadipscing elitr, sed ( )diam nonumy eirmod tempor ( )invidunt ut labore et ( )dolore magna aliquyam erat, sed diam voluptua.(\\n)
''', {
      assert """\
Lorem ipsum

Lorem ipsum\
 dolor sit amet, \
consetetur sadipscing elitr, sed\
\
 diam nonumy eirmod tempor \
\
\
invidunt ut labore et\
 \
\
dolore magna aliquyam erat, sed diam voluptua.\
\
\
""" == """Lorem ipsum
Lorem ipsum \
 dolor sit amet, \
 consetetur sadipscing elitr, sed\
 \
 diam nonumy eirmod tempor \
\
 \
invidunt ut labore et\
 \
 \
dolore magna aliquyam erat, sed diam voluptua.\

\
\
"""
    }
  }

  private StringBuilder largeStringBuilder(CharSequence source = "aaaaaaaaaaaaaaaa", int length = FailedStringComparisonRenderer.MAX_EDIT_DISTANCE_MEMORY / 2) {
    def sb = new StringBuilder(length + 10)
    int cslength = source.length()
    for (int i = 0; i < length; i += cslength) {
      sb.append(source, 0, Math.min(length - i, cslength))
    }
    return sb
  }
}
