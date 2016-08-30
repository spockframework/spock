package org.spockframework.spring.docs

import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import javax.inject.Named

abstract class MockExampleBase extends Specification {

//tag::example[]
  @Autowired @Named('serviceMock')
  GreeterService serviceMock

  @Autowired @Named('serviceStub')
  GreeterService serviceStub

  @Autowired @Named('serviceSpy')
  GreeterService serviceSpy

  def "mock service"() {
    when:
    def result = serviceMock.greeting

    then:
    result == 'mock me'
    1 * serviceMock.getGreeting() >> 'mock me'
  }

  def "sub service"() {
    given:
    serviceStub.getGreeting() >> 'stub me'

    expect:
    serviceStub.greeting == 'stub me'
  }

  def "spy service"() {
    when:
    def result = serviceSpy.greeting

    then:
    result == 'Hello World'
    1 * serviceSpy.getGreeting()
  }
//end::example[]
}
