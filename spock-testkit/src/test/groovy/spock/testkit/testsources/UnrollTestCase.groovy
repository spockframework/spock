package spock.testkit.testsources

import spock.lang.*

class UnrollTestCase extends Specification {

  @Unroll
  def "unrollMe"() {
    expect:
    Math.max(a, b) == c

    where:
    a | b | c
    1 | 2 | 2
    2 | 2 | 2
    3 | 2 | 3
  }


  @Unroll("Max of #a #b == #c")
  def "unrollMe2"() {
    expect:
    Math.max(a, b) == c

    where:
    a | b | c
    1 | 2 | 2
    2 | 2 | 2
    3 | 2 | 3

  }

  @Unroll
  def "Unroll #a #b == #c"() {
    expect:
    Math.max(a, b) == c

    where:
    a | b | c
    1 | 2 | 2
    2 | 2 | 2
    3 | 2 | 3
  }


  def "noExtraReporting"() {
    expect:
    Math.max(a, b) == c

    where:
    a | b | c
    1 | 2 | 2
    2 | 2 | 2
    3 | 2 | 3
  }

}
