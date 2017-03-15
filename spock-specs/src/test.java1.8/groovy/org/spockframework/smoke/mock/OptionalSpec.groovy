package org.spockframework.smoke.mock

import spock.lang.Specification

class OptionalSpec extends Specification {

  def "default answer for Optional should be Optional.empty()"() {
    given:
    TestService service = Stub()

    when:
    Optional<String> result = service.value

    then:
    !result.present
  }

  interface TestService {
    Optional<String> getValue()
  }
}

