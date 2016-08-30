package org.spockframework.spring.docs

import org.springframework.context.annotation.Bean
import spock.mock.DetachedMockFactory

//tag::javaconfig[]
public class DetachedJavaConfig {
  def mockFactory = new DetachedMockFactory()

  @Bean
  GreeterService serviceMock() {
    return mockFactory.Mock(GreeterService)
  }

  @Bean
  GreeterService serviceStub() {
    return mockFactory.Stub(GreeterService)
  }

  @Bean
  GreeterService serviceSpy() {
    return mockFactory.Spy(GreeterServiceImpl)
  }
}
//end::javaconfig[]
