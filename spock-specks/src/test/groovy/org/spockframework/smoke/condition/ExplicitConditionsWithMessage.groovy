package org.spockframework.smoke.condition

import org.junit.runner.RunWith

import spock.lang.*
import static spock.lang.Predef.*
import static org.spockframework.smoke.condition.ConditionSpeckUtil.*
import org.spockframework.runtime.ConditionNotSatisfiedError

/**
 * @author Peter Niederwieser
 */
@Issue("22")
@Speck
@RunWith (Sputnik)
class ExplicitConditionsWithMessage {
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

Message: need to brush up my math
    """, {
      assert 1 + 2 == 2, "need to brush up my math"
    }
  }

  def "rendering of GString message"() {
    expect:
    isRendered """
a + b == 2

Message: a: 1 b: 2
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

Message: [a:1, b:2]
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

Message: null
    """, {
      def x = null
      assert 1 + 2 == 2, x
    }
  }
}