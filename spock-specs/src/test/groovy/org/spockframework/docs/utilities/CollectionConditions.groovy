package org.spockframework.docs.utilities

import org.spockframework.smoke.condition.ConditionRenderingSpec

class CollectionConditions extends ConditionRenderingSpec {

  def 'lenient mismatch'() {
    expect:
    assertRendered({
      // tag::lenient-matcher[]
      def x = [2, 2, 1, 3, 3]
      assert x =~ [4, 1, 2]
      // end::lenient-matcher[]
    },
      /* tag::lenient-matcher-result[] */ """
x =~ [4, 1, 2]
| |
| false
| 2 differences (66% similarity, 1 missing, 1 extra)
| missing: [4]
| extra: [3]
[2, 1, 3]
    """ // end::lenient-matcher-result[]
    )
  }

  def 'strict matcher'() {
    expect:
    assertRendered({
      // tag::strict-matcher[]
      def x = [2, 2, 1, 3, 3]
      assert x ==~ [4, 1, 2]
      // end::strict-matcher[]
    },
      /* tag::strict-matcher-result[] */ """
x ==~ [4, 1, 2]
| |
| false
[2, 2, 1, 3, 3]

Expected: iterable with items [<4>, <1>, <2>] in any order
     but: not matched: <2>
    """ // end::strict-matcher-result[]
    )
  }

  void assertRendered(Closure condition, String expected) {
    isRendered(expected, condition)
  }
}
