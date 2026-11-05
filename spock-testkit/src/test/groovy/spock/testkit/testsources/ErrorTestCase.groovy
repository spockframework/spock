package spock.testkit.testsources

import spock.lang.IgnoreIf
import spock.lang.Specification
import spock.lang.SpockTypeChecked

import static groovy.transform.TypeCheckingMode.SKIP

@SpockTypeChecked(SKIP)
class ErrorTestCase extends Specification {
  @IgnoreIf({ b })
  def "error gets reported properly"() {
    expect: true
  }
}
