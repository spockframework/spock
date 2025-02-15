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

  def "primitive two-dimensional array value"() {
    expect:
    isRendered """
x == y
| |  |
| |  [[1, 2], [1, 5]]
| false
[[1, 2], [2, 5]]
    """, {
      def x = [[1, 2], [2, 5]] as int[][]
      def y = [[1, 2], [1, 5]] as int[][]
      assert x == y
    }
  }

  def "object two-dimensional array value"() {
    expect:
    isRendered """
x == y
| |  |
| |  [[one, two], [one, five]]
| false
[[one, two], [two, five]]
    """, {
      def x = [["one", "two"], ["two", "five"]] as String[][]
      def y = [["one", "two"], ["one", "five"]] as String[][]
      assert x == y
    }
  }

  def "multidimensional array value with higher cardinality"() {
    expect:
    isRendered """
x == y
| |  |
| |  [[[1, 2], [1, 5]], [[1, 2], [1, 5]]]
| false
[[[1, 2], [2, 5]], [[1, 2], [2, 5]]]
    """, {
      def x = [[[1, 2], [2, 5]], [[1, 2], [2, 5]]] as int[][][]
      def y = [[[1, 2], [1, 5]], [[1, 2], [1, 5]]] as int[][][]
      assert x == y
    }
  }

}
