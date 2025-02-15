package org.spockframework.smoke.condition


import static org.spockframework.smoke.condition.ValueRendering.DefaultToString

class ArrayComparisonRendering extends ConditionRenderingSpec {

  def "primitive array value"() {
    expect:
    isRendered """
x == null
| |
| false
[1, 2]
    """, {
      def x = [1, 2] as int[]
      assert x == null
    }
  }

  def "object array value"() {
    expect:
    isRendered """
x == null
| |
| false
[one, two]
    """, {
      def x = ["one", "two"] as String[]
      assert x == null
    }
  }

  def "arrays of variables with default to string is dump()ed"() {
    def a = new DefaultToString()
    def b = new DefaultToString()
    DefaultToString[] x = [a, b]
    expect:
    isRendered """
x == null
| |
| false
[${a.dump()}, ${b.dump()}]
    """, {
      assert x == null
    }
  }

}
