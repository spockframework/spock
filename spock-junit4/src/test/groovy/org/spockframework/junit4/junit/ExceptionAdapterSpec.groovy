package org.spockframework.junit4.junit

import org.opentest4j.MultipleFailuresError

class ExceptionAdapterSpec extends JUnitBaseSpec {

  def "AssumptionViolationException is transformed to TestAbortedException"() {
    when:
    def result = runner.runFeatureBody('''
      expect:
      throw new org.junit.AssumptionViolatedException("skip")
    ''')

    then:
    result.testsAbortedCount == 1
  }

  def "MultipleFailureException is transformed to MultipleFailuresError"() {
    given:
    runner.throwFailure = false

    when:
    def result = runner.runFeatureBody('''
      expect:
      throw new org.junit.runners.model.MultipleFailureException([new RuntimeException("foo"), new RuntimeException("bar")])
    ''')

    then:
    result.testsFailedCount == 1
    result.failures[0].exception instanceof MultipleFailuresError
    (result.failures[0].exception as MultipleFailuresError).failures.message == ['foo', 'bar']
  }
}
