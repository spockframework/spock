package org.spockframework.runtime

import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.UniqueId
import spock.lang.Specification

class ErrorSpecNodeSpec extends Specification {
  def 'should not be pruned'() {
    given:
    TestDescriptor parent = Mock()
    def testee = new ErrorSpecNode(
      UniqueId.forEngine("test"),
      new SpecInfoBuilder(getClass()).build(),
      null)
    testee.setParent(parent)

    when:
    testee.prune()

    then:
    0 * parent.removeChild(*_)
  }
}
