package org.spockframework.runtime.model

import spock.lang.Specification
import spock.lang.Subject

class IterationFilterSpec extends Specification {

  @Subject
  def filter = new IterationFilter()

  def "allows all indexes by default"() {
    expect:
    filter.isAllowed(0)
    filter.isAllowed(1)
  }

  def "allows only selected indexes after one has been allowed explicitly"() {
    when:
    filter.allow(0)

    then:
    filter.isAllowed(0)
    !filter.isAllowed(1)
  }

  def "allows all indexes after configured to do so"() {
    when:
    filter.allow(0)
    filter.allowAll()

    then:
    filter.isAllowed(0)
    filter.isAllowed(1)
  }
}
