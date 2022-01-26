package org.spockframework.spring3

import spock.mock.DetachedMockFactory

import java.util.concurrent.Executor

import groovy.transform.CompileStatic
import org.springframework.context.annotation.*

//@CompileStatic  //TODO: Explain why compilation started to fail with Groovy 4 and all supported JDKs
@Configuration
class TestConfig {
  @Bean
  Executor executor() {
    new DetachedMockFactory().Stub(Executor)
  }
}

