package org.spockframework.spring7

import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

@ContextConfiguration(classes = NoMockConfig)
class SpringBeanTest extends Specification {

  @Autowired
  ServiceExecutor serviceExecutor

  @SpringBean
  ExecutorService executor = Mock()

  def "replace executor"() {
    when:
    def result = serviceExecutor.exec()

    then:
    result == 'mocked'
    1 * executor.submit(_) >> Stub(Future) {
      get() >> 'mocked'
    }
  }
}
