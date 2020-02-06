package spock.testkit.testsources

import spock.lang.*

class ExampleTestCase extends Specification {

  def "first"() {
    expect: true
  }

  def "failMe"() {
    expect: false
  }

  @Ignore
  def "ignoreMe"() {
    expect: false
  }
}
