package spock.testkit.testsources

import spock.lang.*

class SetupCleanupTestCase extends Specification {

  List sut

  def setup() {
    sut = []
  }

  def cleanup() {
    assert !sut.empty
  }

  def "setup works"() {
    when:
    sut.add('foo')

    then:
    sut.size() == 1
  }
}
