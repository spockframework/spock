package org.spockframework.smoke.condition

import static org.hamcrest.CoreMatchers.equalTo
import static spock.util.matcher.MatcherSupport.that

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

