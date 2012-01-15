package org.spockframework.smoke.mock

import spock.lang.Specification

class MockingClosures extends Specification {
  def "can mock call() method"() {
    given:
    Closure c = Mock()

    when:
    c.call()

    then:
    1 * c.call()
    
    when:
    c.call("one")
    
    then:
    1 * c.call("one")
    
    when:
    c.call("one", "two")
    
    then:
    1 * c.call("one", "two")
  }
}
