package org.spockframework.mock

import org.spockframework.util.UnreachableCodeError
import spock.lang.Specification

class DefaultInteractionSpec extends Specification {

  def i = Spy(DefaultInteraction)

  def "default implementation"() {
    expect:
    !i.isCardinalitySpecified()
    i.constraints.isEmpty()
    !i.isThisInteractionOverridableBy(i)
    i.line == -1
    i.column == -1
    i.satisfied
    !i.exhausted
    !i.required
  }

  def "getAcceptedInvocations"() {
    when:
    i.getAcceptedInvocations()
    then:
    thrown(UnreachableCodeError)
  }

  def "computeSimilarityScore"() {
    when:
    i.computeSimilarityScore(null)
    then:
    thrown(UnreachableCodeError)
  }

  def "describeMismatch"() {
    when:
    i.describeMismatch(null)
    then:
    thrown(UnreachableCodeError)
  }
}
