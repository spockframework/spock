package org.spockframework.smoke.mock

import org.spockframework.EmbeddedSpecification
import org.spockframework.mock.TooFewInvocationsError

class UnmatchedInvocations extends EmbeddedSpecification {
  def "are shown when interaction isn't satisfied"() {
    when:
    runner.runFeatureBody """
def list = Mock(List)

when:
list.add("elem")

then:
1 * list.remove(0)
    """

    then:
    TooFewInvocationsError e = thrown()
    e.unmatchedInvocations.size() == 1
    def invocation = e.unmatchedInvocations[0]
    invocation.method.name == "add"
  }

  def "different add calls"() {
    when:
    runner.runFeatureBody """
def list = Mock(List)

when:
list.add("elem")

then:
1 * list.add("any")
    """

    then:
    TooFewInvocationsError e = thrown()
    e.unmatchedInvocations.size() == 1
    def invocation = e.unmatchedInvocations[0]
    invocation.method.name == "add"
    invocation.arguments[0] == "elem"
  }

  def "all unmatched calls are caught"() {
    when:
    runner.runFeatureBody """
def list = Mock(List)

when:
list.add("elem")
list.add("other")
list.remove(1)
list.add("one")

then:
1 * list.add("any")
    """

    then:
    TooFewInvocationsError e = thrown()
    e.unmatchedInvocations.size() == 4
    def invocation = e.unmatchedInvocations[0]
    invocation.arguments[0] == "elem"
  }

  def "check output string"() {
    when:
    runner.runFeatureBody """
def list = Mock(List)

when:
list.add("elem")

then:
1 * list.add("any")
    """

    then:
    TooFewInvocationsError e = thrown()
    e.unmatchedInvocations.size() == 1
    e.message == """Too few invocations for:

1 * list.add("any")   (0 invocations)

Unmatched invocations:

list.add('elem')
"""
  }

  def "isn't thrown when interactions are satisfied"() {
    when:
    runner.runFeatureBody """
def list = Mock(List)

when:
list.add("elem")
list.remove(0)

then:
1 * list.add("elem")
    """

    then:
    noExceptionThrown()
  }

}
