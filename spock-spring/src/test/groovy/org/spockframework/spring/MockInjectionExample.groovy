package org.spockframework.spring

import org.spockframework.mock.MockUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.inject.Named

@ContextConfiguration(locations = "MockExamples-context.xml")
class MockInjectionExample extends Specification {

  @Autowired @Named('serviceMock')
  IService1 serviceMock


  @Autowired @Named('serviceStub')
  IService1 serviceStub

  @Autowired @Named('serviceSpy')
  IService2 serviceSpy

  @Autowired @Named('service2')
  IService2 service2

  def "Injected services are mocks"() {
    expect:
    new MockUtil().isMock(serviceMock)
    new MockUtil().isMock(serviceStub)
    new MockUtil().isMock(serviceSpy)
    new MockUtil().isMock(service2)
  }

  def "Mocks can be configured"() {
    given:
    serviceStub.generateString() >> "I can be configured"

    when:
    assert serviceMock.generateString() == "I can be configured"
    assert serviceStub.generateString() == "I can be configured"
    assert serviceSpy.generateQuickBrownFox() == "I can be configured"
    assert service2.generateQuickBrownFox() == "The quick brown fox..."

    then:
    1 * serviceMock.generateString() >> "I can be configured"
    1 * serviceSpy.generateQuickBrownFox() >> "I can be configured"
    1 * service2.generateQuickBrownFox() >> "The quick brown fox..."
  }

  def "Unconfigured mocks return default"() {
    expect:
    serviceMock.generateString() == null
    serviceStub.generateString() == ""
    serviceSpy.generateQuickBrownFox() == "The quick brown fox jumps over the lazy dog."
    service2.generateQuickBrownFox() == null

  }

}
