package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification

import static org.junit.platform.testkit.engine.EventConditions.*
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message

class PendingFeatureExtensionSpec extends EmbeddedSpecification {

  def "@PendingFeature marks failing feature as skipped"() {
    when:
    def result = runner.runWithImports("""

class Foo extends Specification {
  @PendingFeature
  def bar() {
    expect: false
  }
}
    """)

    then:
    notThrown(AssertionError)
    result.testsStartedCount == 1
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
    result.testsAbortedCount == 1
  }

  def "@PendingFeature includes reason in exception message"() {
    when:
    def result = runner.runWithImports("""

class Foo extends Specification {
  @PendingFeature(reason='42')
  def bar() {
    expect: false
  }
}
    """)

    then:
    notThrown(AssertionError)
    result.testsStartedCount == 1
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
    result.testsAbortedCount == 1
    result.tests().aborted().assertEventsMatchExactly(abortedWithReason(message("Feature not yet implemented correctly. Reason: 42")))
  }

  def "@PendingFeature marks feature that fails with exception as skipped"() {
    when:
    def result = runner.runWithImports("""

class Foo extends Specification {
  @PendingFeature
  def bar() {
    expect:
    throw new Exception()
  }
}
    """)

    then:
    noExceptionThrown()
    result.testsStartedCount == 1
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
    result.testsAbortedCount == 1
  }

  def "@PendingFeature rethrows non handled exceptions"() {
    when:
    def result = runner.runWithImports("""

class Foo extends Specification {
  @PendingFeature(exceptions=[IndexOutOfBoundsException])
  def bar() {
    expect:
    throw new IllegalArgumentException()
  }
}
    """)

    then:
    thrown(IllegalArgumentException)
  }

  def "@PendingFeature marks passing feature as failed"() {
    when:
    runner.runWithImports("""

class Foo extends Specification {
  @PendingFeature
  def bar() {
    expect: true
  }
}
    """)

    then:
    AssertionError e = thrown(AssertionError)
    e.message == "Feature is marked with @PendingFeature but passes unexpectedly"
  }

  def "@PendingFeature marks data driven feature where every iteration fails as skipped"() {
    when:
    def result = runner.runWithImports("""

class Foo extends Specification {
  @PendingFeature
  def bar() {
    expect: test

    where:
    test << [false, false, false]
  }
}
    """)

    then:
    notThrown(AssertionError)
    result.testsSucceededCount == 3
    result.testsFailedCount == 0
    result.testsSkippedCount == 0

    // TODO add skipped check on refactoring of Result object
  }


  def "@PendingFeature marks data driven feature where at least one iteration fails as skipped"() {
    when:
    def result = runner.runWithImports("""

class Foo extends Specification {
  @PendingFeature
  def bar() {
    expect: test

    where:
    test << [true, false, true]
  }
}
    """)

    then:
    notThrown(AssertionError)
    result.testsSucceededCount == 3
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
  }


  def "@PendingFeature marks data driven feature where all iterations pass as failed"() {
    when:
    runner.runWithImports("""

class Foo extends Specification {
  @PendingFeature
  def bar() {
    expect: test

    where:
    test << [true, true, true]
  }
}
    """)

    then:
    AssertionError e = thrown(AssertionError)
    e.message == "Feature is marked with @PendingFeature but passes unexpectedly"
  }
}
