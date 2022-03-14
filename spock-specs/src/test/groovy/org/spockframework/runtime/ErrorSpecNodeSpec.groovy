package org.spockframework.runtime

import spock.lang.*

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

  @Issue("https://github.com/spockframework/spock/issues/1444")
  def 'make TestDescriptor think that an ErrorSpecNode contains tests'() {
    given:
    def errorSpecNodeSpy = Spy(errorSpecNode)

    when:
    def containsTests = TestDescriptor.containsTests(errorSpecNodeSpy)

    then:
    containsTests
    1 * errorSpecNodeSpy.mayRegisterTests()
  }
}
