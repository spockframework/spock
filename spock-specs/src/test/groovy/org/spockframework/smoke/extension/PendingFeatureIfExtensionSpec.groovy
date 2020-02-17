package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.ConditionNotSatisfiedError

class PendingFeatureIfExtensionSpec extends EmbeddedSpecification {
  def "@PendingFeatureIf marks failing feature as skipped if the condition passes and the test fails"() {
    when:
    def result = runner.runSpecBody """
@PendingFeatureIf({true})
def bar() {
  expect: false
}
"""

    then:
    notThrown(AssertionError)
    result.testsStartedCount == 1
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
  }

  def "@PendingFeatureIf marks passing feature as failed if the conditional expression returns true"() {
    when:
    runner.runSpecBody """
@PendingFeatureIf({true})
def bar() {
  expect: true
}
"""

    then:
    AssertionError e = thrown()
    e.message == "Feature is marked with @PendingFeatureIf but passes unexpectedly"
  }

  def "@PendingFeatureIf marks failing feature as failed if the conditional expression returns false"() {
    when:
    runner.runSpecBody """
@PendingFeatureIf({false})
def bar() {
  expect: false
}
"""

    then:
    ConditionNotSatisfiedError e = thrown()
    e.condition.values[0] == false
  }

  def "@PendingFeatureIf marks passing feature as passed if the conditional expression returns false"() {
    when:
    def result = runner.runSpecBody """
@PendingFeatureIf({false})
def bar() {
  expect: true
}
"""

    then:
    notThrown(AssertionError)
    result.testsStartedCount == 1
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
  }
}
