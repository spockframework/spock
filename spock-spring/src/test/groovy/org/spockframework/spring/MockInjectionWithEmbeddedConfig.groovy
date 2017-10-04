package org.spockframework.spring

import org.spockframework.mock.MockUtil
import spock.lang.Specification
import spock.mock.DetachedMockFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.*
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.AnnotationConfigContextLoader

@ContextConfiguration(loader=AnnotationConfigContextLoader)
class MockInjectionWithEmbeddedConfig extends Specification {

  @Configuration
  static class Config {
    private DetachedMockFactory factory = new DetachedMockFactory()

    @Bean
    IService1 service1() {
        return factory.Mock(IService1, name: "service1")
    }

    @Bean
    IService2 service2() {
        return factory.Mock(IService2, name: "service2")
    }
  }

  @Autowired
  IService1 service1

  @Autowired
  IService2 service2

  List normalMock = Mock()

  def "Injected services are mocks"() {
    expect:
    new MockUtil().isMock(service1)
    new MockUtil().isMock(service2)
  }

  def "Mocks can be configured"() {
    when:
    assert service1.generateString() == "I can be configured"
    assert service2.generateQuickBrownFox() == "The quick brown fox..."

    then:
    1 * service1.generateString() >> "I can be configured"
    1 * service2.generateQuickBrownFox() >> "The quick brown fox..."
  }

  def "Unconfigured mocks return default"() {
    expect:
    service1.generateString() == null
    service2.generateQuickBrownFox() == null
  }

  def "normal mocks work too"() {
    when:
    def result = normalMock.get(0)

    then:
    result == "hello"
    1 * normalMock.get(_) >> 'hello'
  }
}
