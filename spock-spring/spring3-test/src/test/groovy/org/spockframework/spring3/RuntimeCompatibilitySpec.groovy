package org.spockframework.spring3

import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.AnnotationConfigContextLoader
import spock.lang.Specification

@ContextConfiguration(loader = AnnotationConfigContextLoader)
class RuntimeCompatibilitySpec extends Specification {

  def "no runtime errors are thrown"() {
    expect:
    1 == 1
  }

}
