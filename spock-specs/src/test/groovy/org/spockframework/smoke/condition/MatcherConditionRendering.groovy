/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.condition

import static org.hamcrest.CoreMatchers.equalTo
import static spock.util.matcher.HamcrestSupport.that

class MatcherConditionRendering extends ConditionRenderingSpec {
  @IsRendered("""
x equalTo(43)
| |
| false
42

Expected: <43>
     but: was <42>
  """)
  def "short syntax"() {
    def x = 42

    expect:
    x equalTo(43)
  }

  @IsRendered("""
that x, equalTo(43)
|    |
|    42
false

Expected: <43>
     but: was <42>
  """)
  def "long syntax"() {
    def x = 42

    expect:
    that x, equalTo(43)
  }

  @IsRendered("""
that(x, equalTo(43))
|    |
|    42
false

Expected: <43>
     but: was <42>
  """)
  def "explicit condition"() {
    def x = 42

    expect:
    assert that(x, equalTo(43))
  }

  @IsRendered("""
that(x, equalTo(43))
|    |
|    42
false

oops!

Expected: <43>
     but: was <42>
  """)
  def "custom message"() {
    def x = 42

    expect:
    assert that(x, equalTo(43)), "oops!"
  }
}

