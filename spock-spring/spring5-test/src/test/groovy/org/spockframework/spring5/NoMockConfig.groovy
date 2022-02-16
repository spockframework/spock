package org.spockframework.spring5

import groovy.transform.CompileStatic
import org.springframework.context.annotation.Bean

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService

@CompileStatic
class NoMockConfig {

  @Bean
  ExecutorService executor() {
    throw new RuntimeException("This should not be called")
  }

  @Bean
  ServiceExecutor serviceExecutor(ExecutorService executorService) {
    return new ServiceExecutor(executorService)
  }


}

class ServiceExecutor {
  private final ExecutorService executorService

  ServiceExecutor(ExecutorService executorService) {
    this.executorService = executorService
  }

  def exec() {
    executorService.submit({"done"} as Callable).get()
  }
}
