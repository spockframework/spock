package org.spockframework.junit4.junit

import spock.lang.Issue

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

  @Issue("https://github.com/spockframework/spock/issues/1263")
  def "AssumptionViolationException is transformed to TestAbortedException for inherited specs"() {
    given:
    runner.throwFailure = false

    when:
    def result = runner.runWithImports("""
      class SuperSpec extends Specification {
          def $fixtureMethod() {
            throw new org.junit.AssumptionViolatedException("skip")
          }

          def "super feature"() {
              expect:
              true
          }
      }
      class SubSpec extends SuperSpec {
          def "sub feature"() {
              expect:
              true
          }
      }
    """)

    then:
    verifyAll(result) {
      testsStartedCount == testsStarted
      testsAbortedCount == testsAborted
      testsSucceededCount == testsSucceeded
      containersAbortedCount == containersAborted
      containersStartedCount == 3 // engine + 2 specs
      testsFailedCount == 0
    }

    where:
    fixtureMethod | testsStarted | testsAborted | testsSucceeded | containersAborted
    'setup'       | 3            | 3            | 0              | 0
    'cleanup'     | 3            | 3            | 0              | 0
    'setupSpec'   | 0            | 0            | 0              | 2
    'cleanupSpec' | 3            | 0            | 3              | 2
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
