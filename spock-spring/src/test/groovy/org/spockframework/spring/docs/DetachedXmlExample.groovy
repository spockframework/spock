package org.spockframework.spring.docs

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration

import javax.inject.Named

@ContextConfiguration(locations = "classpath:org/spockframework/spring/docs/MockDocu-context.xml")
class DetachedXmlExample extends MockExampleBase {

  @Autowired @Named('alternativeMock')
  GreeterService alternativeMock

  def "alternatice mock service"() {
    when:
    def result = alternativeMock.greeting

    then:
    result == 'mock me'
    1 * alternativeMock.getGreeting() >> 'mock me'
  }

}
