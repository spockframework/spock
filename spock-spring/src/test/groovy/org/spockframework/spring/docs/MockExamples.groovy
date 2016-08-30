package org.spockframework.spring.docs

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
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

  @Autowired @Named('alternativeMock')
  GreeterService alternativeMock

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

  def "alternatice mock service"() {
    when:
    def result = alternativeMock.greeting

    then:
    result == 'mock me'
    1 * alternativeMock.getGreeting() >> 'mock me'
  }
//end::example[]
}


@ContextConfiguration(classes = DetachedJavaConfig)
class DetachedJavaConfigExample extends MockExampleBase {
}


@ContextConfiguration(locations = "classpath:org/spockframework/spring/docs/MockDocu-context.xml")
class DetachedXmlExample extends MockExampleBase {
}
