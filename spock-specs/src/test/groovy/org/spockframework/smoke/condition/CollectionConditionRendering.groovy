package org.spockframework.smoke.condition

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
| false
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
