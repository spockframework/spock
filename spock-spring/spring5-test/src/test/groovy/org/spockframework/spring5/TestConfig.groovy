package org.spockframework.spring5

import groovy.transform.CompileStatic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import spock.mock.DetachedMockFactory

import java.util.concurrent.Executor

//@CompileStatic  //TODO: Explain why compilation started to fail with Groovy 4, but only with JDK 8
@Configuration
class TestConfig {
  @Bean
  Executor executor() {
    new DetachedMockFactory().Stub(Executor)
  }
}

