package org.spockframework.spring5

import spock.lang.Specification

import java.util.concurrent.Executor

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(classes = TestConfig)
class RuntimeCompatibilitySpec extends Specification {

  @Autowired
  Executor injectMe

  def "no runtime errors are thrown"() {
    expect:
    1 == 1
  }

  def "injection works"() {
    expect:
    injectMe != null
  }
}
