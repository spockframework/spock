package org.spockframework.smoke.mock

import spock.lang.Specification
import spock.lang.Issue
import spock.lang.FailsWith
import org.spockframework.mock.TooManyInvocationsError

@Issue("https://github.com/spockframework/spock/issues/127")
class OverlappingInteractions extends Specification {
  def "invocations are spread over identical interactions"() {
    def list = Mock(List)

    when:
    list.add(1)
    list.add(2)
    list.add(2)
    list.add(1)

    then:
    1 * list.add(1)
    2 * list.add(2)
    1 * list.add(1)
  }

  def "invocations are spread over overlapping interactions"() {
    def list = Mock(List)

    when:
    list.add(1)
    list.add(2)
    list.add(2)
    list.add(1)

    then:
    1 * list.add(!0)
    2 * list.add(2)
    1 * list.add(_)
  }

  // this use case is especially important
  def "invocations are spread over overlapping ordered interactions"() {
    def list = Mock(List)

    when:
    list.add(1)
    list.add(2)
    list.add(2)
    list.add(1)

    then:
    1 * list.add(!0)

    then:
    2 * list.add(2)

    then:
    1 * list.add(_)
  }

  @FailsWith(TooManyInvocationsError)
  def "invocations are not spread over overlapping interactions in different scopes"() {
    def list = Mock(List)

    1 * list.add(1)

    when:
    list.add(1)

    then:
    0 * list.add(1)
  }
}
