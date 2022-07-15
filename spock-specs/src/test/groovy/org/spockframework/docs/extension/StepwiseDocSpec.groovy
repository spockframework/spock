package org.spockframework.docs.extension

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.extension.builtin.PreconditionContext
import spock.lang.IgnoreIf

class StepwiseDocSpec extends EmbeddedSpecification {
  def "Annotation on spec"() {
    runner.throwFailure = false

    when:
    def result = runner.runWithImports(/* tag::example-a[] */"""
      @Stepwise
      class RunInOrderSpec extends Specification {
        def "I run first"()  { expect: true }
        def "I run second"() { expect: false }
        def "I will be skipped"() { expect: true }
      }
    """) // end::example-a[]

    then:
    result.testsSucceededCount == 1
    result.testsStartedCount == 2
    result.testsFailedCount == 1
    result.testsSkippedCount == 1
  }

  def "Annotation on feature"() {
    runner.throwFailure = false

    when:
    def result = runner.runWithImports(/* tag::example-b[] */"""
      class SkipAfterFailingIterationSpec extends Specification {
        @Stepwise
        def "iteration #count"()  {
          expect:
          count != 3

          where:
          count << (1..5)
        }
      }
    """) // end::example-b[]

    then:
    result.testsStartedCount == 3 + 1
    result.testsSucceededCount == 2 + 1
    result.testsFailedCount == 1
    result.testsSkippedCount == 2
  }
}
