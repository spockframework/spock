package org.spockframework.spring.mock

import org.spockframework.spring.*
import spock.lang.Specification

import java.util.concurrent.Executor

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ContextConfiguration

@StubBeans([Service2, Executor])
@ContextConfiguration(classes = OverrideMockContext)
class StubBeansOverrideSpec extends Specification {

  @SpringBean
  Service2 service2 = Mock()

  @Autowired
  Service1 service1

  @Autowired
  Executor executor

  def "@StubBeans replace @Bean"() {
    expect:
    executor != null
  }

  def "@StubBeans can be replaced by @SpringBean"() {
    when:
    service1.generateString()

    then:
    1 * service2.generateQuickBrownFox() >> 'hello'
  }

  static class OverrideMockContext {
    @Bean
    Executor executor(Runnable runnable) {
      return null
    }

    @Bean
    IService1 service1(Service2 service2) {
      return new Service1(service2)
    }
  }
}
