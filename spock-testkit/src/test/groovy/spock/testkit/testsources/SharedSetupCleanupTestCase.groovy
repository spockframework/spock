package spock.testkit.testsources

import spock.lang.Shared
import spock.lang.Specification

class SharedSetupCleanupTestCase extends Specification {

  @Shared
  List sut

  def setupSpec() {
    sut = []
  }

  def cleanupSpec() {
    assert !sut.empty
  }

  def "setup works"() {
    when:
    sut.add('foo')

    then:
    sut.size() == 1
  }
}
