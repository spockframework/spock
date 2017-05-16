package org.spockframework.spring2

import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration
class RuntimeCompatibilitySpec extends Specification {

  def "no runtime errors are thrown"() {
    expect:
    1 == 1
  }

}
