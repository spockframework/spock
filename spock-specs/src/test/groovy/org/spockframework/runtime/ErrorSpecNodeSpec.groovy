package org.spockframework.runtime

import spock.lang.Specification

import org.junit.platform.engine.*

class ErrorSpecNodeSpec extends Specification {
  def 'should not be pruned'() {
    given:
    TestDescriptor parent = Mock()
    def testee = new ErrorSpecNode(
      UniqueId.forEngine("test"),
      null,
      new SpecInfoBuilder(getClass()).build(),
      null)
    testee.setParent(parent)

    when:
    testee.prune()

    then:
    0 * parent.removeChild(*_)
  }
}
