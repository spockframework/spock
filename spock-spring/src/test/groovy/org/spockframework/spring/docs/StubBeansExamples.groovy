package org.spockframework.spring.docs

import org.spockframework.spring.Service2
import org.spockframework.spring.StubBeans
import org.spockframework.spring.mock.DemoMockContext
import spock.lang.Specification

import org.springframework.test.context.ContextConfiguration

//tag::example[]
@StubBeans(Service2)
@ContextConfiguration(classes = DemoMockContext)
class StubBeansExamples extends Specification {
//end::example[]

  def "context loads"() {
    expect: true
  }
}
