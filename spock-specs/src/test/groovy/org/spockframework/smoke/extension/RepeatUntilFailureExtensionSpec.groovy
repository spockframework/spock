package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification

class RepeatUntilFailureExtensionSpec extends EmbeddedSpecification {
  def "repeats feature until the first failure"() {
    given:
    runner.throwFailure = false

    when:
    def result = runner.runSpecBody """
      @Shared
      int count = 0

      @RepeatUntilFailure
      def "test"() {
        expect:
        ++count < 3
      }
    """

    then:
    result.testsStartedCount == 1 + 3
    result.testsSucceededCount == 1 + 2
    result.testsFailedCount == 1
  }

  def "repeats until maxAttempts is reached"() {
    when:
    def result = runner.runSpecBody """
      @RepeatUntilFailure(maxAttempts = 3)
      def "test"() {
        expect:
        true
      }
    """

    then:
    result.testsStartedCount == 1 + 3
    result.testsSucceededCount == 1 + 3
    result.testsFailedCount == 0
  }

  def "skips other tests in the same class that do not have the same annotation too"() {
    when:
    def result = runner.runSpecBody """
      def "other test 1"() {
        expect:
        true
      }

      @RepeatUntilFailure(maxAttempts = 1)
      def "other test 2"() {
        expect:
        true
      }

      @RepeatUntilFailure(maxAttempts = 1)
      def "test"() {
        expect:
        true
      }

      def "other test 3"() {
        expect:
        true
      }
    """

    then:
    result.testsStartedCount == 2 + 2
    result.testsSucceededCount == 2 + 2
    result.testsFailedCount == 0
    result.testsSkippedCount == 2
  }


  def "ignores aborted tests"() {
    when:
    def result = runner.runSpecBody """
      @Shared
      int count = 0

      @RepeatUntilFailure(maxAttempts = 3)
      def "test"() {
        given:
        if (++count == 2) {
          throw new org.opentest4j.TestAbortedException("abort")
        }
        expect:
        true
      }
    """

    then:
    result.testsStartedCount == 1 + 3
    result.testsSucceededCount == 1 + 2
    result.testsFailedCount == 0
    result.testsAbortedCount == 1
  }
}
