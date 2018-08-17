package org.spockframework.spring.mock

import org.spockframework.spring.*
import org.spockframework.spring.mock.imported.example.*
import spock.lang.Specification

import java.lang.reflect.UndeclaredThrowableException

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ContextConfiguration

@StubBeans(Service2)
@ContextConfiguration(classes = ExampleContext)
class SpringBeansWithExceptionsSpec extends Specification {

  @SpringBean
  ServiceWithDeclaredException service = Mock()

  @Autowired
  Consumer consumer

  def "mock throws declared checked exception"() {
    when:
    consumer.work()

    then:
    1 * service.doWork() >> { throw new DeclaredException() }
    thrown(DeclaredException)
  }

  def "mock throws undeclared checked exception will cause UndeclaredThrowableException"() {
    when:
    consumer.work()

    then:
    1 * service.doWork() >> { throw new UndeclaredException() }
    thrown(UndeclaredThrowableException)
  }
}

@CompileStatic
class ExampleContext {

  @Bean
  Consumer consumer() {
    new Consumer()
  }
}

@CompileStatic
class Consumer {
  @Autowired
  ServiceWithDeclaredException service

  void work() {
    service.doWork();
  }
}
