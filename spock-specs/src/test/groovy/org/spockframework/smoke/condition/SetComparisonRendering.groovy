package org.spockframework.smoke.condition

class SetComparisonRendering extends ConditionRenderingSpec {

  def "Set differences are displayed"() {
    expect:
    isRendered('''
a == b
| |  |
| |  [2, 3, 4]
| false
| 2 differences (66% similarity missing 1 extra 1)
| missing: [4]
| extra: [1]
[1, 2, 3]
''') {
      Set a = [1, 2, 3]
      Set b = [2, 3, 4]
      assert a == b
    }
  }

  def "Set difference is displayed"() {
    expect:
    isRendered('''
a == b
| |  |
| |  [1, 2, 3, 4]
| false
| 1 difference (75% similarity missing 1 extra 0)
| missing: [4]
| extra: []
[1, 2, 3]
''') {
      Set a = [1, 2, 3]
      Set b = [1, 2, 3, 4]
      assert a == b
    }
  }

  def "Set differences for larger Set with small differences"() {
    expect:
    isRendered('''
a == b
| |  |
| |  [6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20]
| false
| 10 differences (66% similarity missing 5 extra 5)
| missing: [16, 17, 18, 19, 20]
| extra: [1, 2, 3, 4, 5]
[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
''') {
      Set a = (1..15) as Set
      Set b = (6..20) as Set
      assert a == b
    }
  }

  def "Set differences for larger Set with large differences"() {
    expect:
    isRendered('''
a == b
| |  |
| |  [16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30]
| false
| 30 differences (0% similarity missing 15 extra 15 details omitted)
[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
''') {
      Set a = (1..15) as Set
      Set b = (16..30) as Set
      assert a == b
    }
  }

  def "Set differences for contained sets"() {
    expect:
    isRendered('''

a == b
| |  |
| |  [5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20]
| false
| 10 differences (37% similarity missing 10 extra 0)
| missing: [5, 6, 7, 8, 9, 16, 17, 18, 19, 20]
| extra: []
[10, 11, 12, 13, 14, 15]
''') {
      Set a = (10..15) as Set
      Set b = (5..20) as Set
      assert a == b
    }
  }

  def "Set differences for containing sets"() {
    expect:
    isRendered('''
a == b
| |  |
| |  [10, 11, 12, 13, 14, 15]
| false
| 10 differences (37% similarity missing 0 extra 10)
| missing: []
| extra: [5, 6, 7, 8, 9, 16, 17, 18, 19, 20]
[5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20]
''') {
      Set a = (5..20) as Set
      Set b = (10..15) as Set
      assert a == b
    }
  }
}
