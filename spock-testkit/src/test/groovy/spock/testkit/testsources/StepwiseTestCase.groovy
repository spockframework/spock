package spock.testkit.testsources

import spock.lang.*

@Stepwise
class StepwiseTestCase extends Specification {

  @Shared
  int counter = 0

  def setup() {
    counter++
  }

  def "first"() {
    expect: counter == 1
  }

  def "second"() {
    expect: counter == 2
  }

  def "third"() {
    expect: counter == 3
  }

  def "fail"() {
    expect: false
  }

  def "fifth"() {
    expect: false
  }
}
