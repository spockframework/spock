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
}
