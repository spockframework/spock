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

import org.spockframework.runtime.ExpressionInfoValueRenderer
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
             |               |
             |               false
             false
             1 difference (88% similarity)
             the qui(c)k
             the qui(r)k
    """, {
      assert ("the quick" == "the quirk") instanceof String
    }
  }

  @Issue("http://issues.spockframework.org/detail?id=252")
  def "does not show differences if strings in subexpression are equal"() {
    expect:
    isRendered """
("the quick" == "the quick") instanceof String
             |               |
             true            false
    """, {
      assert ("the quick" == "the quick") instanceof String
    }
  }
  @Issue("https://github.com/spockframework/spock/issues/121")
  def "large String comparision does not cause OOM-Error, difference at start"() {
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
  def "large String comparision does not cause OOM-Error, difference in the middle"() {
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
  def "large String comparision does not cause OOM-Error, difference at end"() {
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
  def "large String comparision does not cause OOM-Error, difference at start and  end"() {
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
  def "large String comparision does not cause OOM-Error, complete difference"() {
    String a = largeStringBuilder()
    String b = largeStringBuilder("bbbbbbbbbbbbbbbb")


    expect:
    renderedConditionContains({
      assert a == b
    },
      "false",
      "Strings too large to calculate edit distance.")
  }

  private StringBuilder largeStringBuilder(CharSequence source = "aaaaaaaaaaaaaaaa") {
    int length = ExpressionInfoValueRenderer.MAX_EDIT_DISTANCE_MEMORY / 2
    def sb = new StringBuilder(length + 10)
    int cslength = source.length()
    for (int i = 0; i < length; i += cslength) {
      sb.append(source, 0, Math.min(length - i, cslength))
    }
    return sb
  }
}
