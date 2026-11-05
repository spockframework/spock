package spock.testkit.testsources

import spock.lang.*

class UnrollTestCase extends Specification {

  def "unrollMe"(int a, int b) {
    expect:
    Math.max(a, b) == c

    where:
    a | b | c
    1 | 2 | 2
    2 | 2 | 0
    3 | 2 | 3
  }


  @Unroll("Max of #a #b == #c")
  def "unrollMe2"(int a, int b) {
    expect:
    Math.max(a, b) == c

    where:
    a | b | c
    1 | 2 | 2
    2 | 2 | 2
    3 | 2 | 3
  }

  def "Unroll #a #b == #c"(int a, int b) {
    expect:
    Math.max(a, b) == c

    where:
    a | b | c
    1 | 2 | 2
    2 | 2 | 2
    3 | 2 | 3
  }

  @Rollup
  def "noExtraReporting"(int a, int b) {
    expect:
    Math.max(a, b) == c

    where:
    a | b | c
    1 | 2 | 2
    2 | 2 | 2
    3 | 2 | 3
  }

}
