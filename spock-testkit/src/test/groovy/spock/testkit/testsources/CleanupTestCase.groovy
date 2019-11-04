package spock.testkit.testsources


import spock.lang.Specification

class CleanupTestCase extends Specification {

  List sut

  def cleanup() {
    assert sut.empty
  }

  def "setup works"() {
    given:
    sut = []
    when:
    sut.add('foo')

    then:
    sut.size() == 1

    cleanup:
    sut.clear()
  }
}
