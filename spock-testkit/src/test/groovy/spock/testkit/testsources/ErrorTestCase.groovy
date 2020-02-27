package spock.testkit.testsources

import spock.lang.IgnoreIf
import spock.lang.Specification

class ErrorTestCase extends Specification {
  @IgnoreIf({ b })
  def "error gets reported properly"() {
    expect: true
  }
}
