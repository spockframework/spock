package org.spockframework.smoke.mock

import spock.lang.Specification

class GroovyMocks extends Specification {
  def "automatically implement GroovyObject"() {
    expect:
    GroovyMock(List) instanceof GroovyObject
    GroovyMock(ArrayList) instanceof GroovyObject
  }
}
