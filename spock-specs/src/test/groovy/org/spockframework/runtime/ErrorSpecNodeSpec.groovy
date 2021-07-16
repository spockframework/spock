package org.spockframework.runtime

import spock.lang.Specification

import org.junit.platform.engine.*
import spock.lang.Subject

class ErrorSpecNodeSpec extends Specification {
  TestDescriptor tdParent = Mock()

  @Subject
  def errorSpecNode = new ErrorSpecNode(
    UniqueId.forEngine("test"),
    null,
    new SpecInfoBuilder(getClass()).build(),
    null).tap {
    parent = tdParent
  }


  def 'should not be pruned'() {
    when:
    errorSpecNode.prune()

    then:
    0 * tdParent.removeChild(*_)
  }

  def 'should prevent removal of ErrorSpecNode'() {
    when:
    errorSpecNode.removeFromHierarchy()

    then:
    0 * tdParent.removeChild(*_)
  }
}
