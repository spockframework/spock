package org.spockframework.smoke.condition

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.GroovyRuntimeUtil
import spock.lang.Issue
import spock.lang.Requires

class ConditionG4Spec extends EmbeddedSpecification {

  @Issue("https://github.com/spockframework/spock/issues/1956")
  def "test range"() {
    expect:
    (0..5) == [0, 1, 2, 3, 4, 5]
    (0<..5) == [1, 2, 3, 4, 5]
    (0..<5) == [0, 1, 2, 3, 4]
    (0<..<5) == [1, 2, 3, 4]
  }

  // Groovy 6 no longer accepts a statement like `assert` as the body of an arrow switch expression branch,
  // so the snippet is compiled at runtime to keep this spec compiling on Groovy 6
  @Issue("https://github.com/spockframework/spock/issues/1845")
  @Requires({ GroovyRuntimeUtil.MAJOR_VERSION < 6 })
  def "explicit assert in switch expression"() {
    when:
    def result = runner.runFeatureBody '''
expect:
def b = 3
!!switch (b) {
  case 3 -> assert 1 == 1
  default -> assert 1 == 1
}
'''

    then:
    result.testsSucceededCount == 1
  }
}
