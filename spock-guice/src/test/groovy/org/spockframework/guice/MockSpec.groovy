package org.spockframework.guice

import spock.guice.UseModules
import spock.lang.Specification

import javax.inject.Inject

@UseModules(MockModule)
class MockSpec extends Specification {

  @Inject
  IService1 service1

  @Inject
  IService2 service2

  def "mocks and stubs are auto attached on injection" () {
    given:
    service1.generateString() >> 'hello'
    service2.generateQuickBrownFox() >> 'world'

    expect:
    service1.generateString() == 'hello'
    service2.generateQuickBrownFox() == 'world'
  }


  def "mocking works as well" () {
    when:
    service1.generateString() == 'hello'

    then:
    1 * service1.generateString() >> 'hello'
  }
}
