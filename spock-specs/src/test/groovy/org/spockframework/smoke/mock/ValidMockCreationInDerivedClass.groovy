package org.spockframework.smoke.mock

import spock.lang.Specification

class ValidMockCreationInDerivedClass extends ValidMockCreationInDerivedClassBase {
  def map = Mock(Map)

  def "all mocks have been created"() {
    expect:
    list instanceof List
    map instanceof Map
  }
}

abstract class ValidMockCreationInDerivedClassBase extends Specification {
  List list = Mock()
}


