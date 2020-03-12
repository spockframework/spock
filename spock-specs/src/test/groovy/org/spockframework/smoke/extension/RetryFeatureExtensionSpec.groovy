package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.ConditionNotSatisfiedError
import spock.lang.*

import java.util.concurrent.atomic.AtomicInteger

import org.opentest4j.MultipleFailuresError

class RetryFeatureExtensionSpec extends EmbeddedSpecification {

  static AtomicInteger setupCounter = new AtomicInteger()
  static AtomicInteger cleanupCounter = new AtomicInteger()
  static AtomicInteger featureCounter = new AtomicInteger()

  def setup() {
    runner.addClassImport(Retry)
    runner.addClassMemberImport(getClass())
    runner.throwFailure = false
    featureCounter.set(0)
  }

  def "@Retry fails after retries are exhausted"() {
    when:
    def result = runner.runSpecBody("""
@Retry
def bar() {
  featureCounter.incrementAndGet()
  expect: false
}
    """)

    then:
    result.testsStartedCount == 1
    result.testsSucceededCount == 0
    result.testsFailedCount == 1
    with(result.failures.exception[0], MultipleFailuresError) {
      failures.size() == 4
      failures.every { it instanceof ConditionNotSatisfiedError }
    }
    result.testsSkippedCount == 0
    featureCounter.get() == 4
  }

  def "@Retry works for normal exceptions"() {
    when:
    def result = runner.runSpecBody("""
@Retry
def bar() {
  featureCounter.incrementAndGet()
  expect:
  throw new IOException()
}
    """)

    then:
    result.testsStartedCount == 1
    result.testsSucceededCount == 0
    result.testsFailedCount == 1
    with(result.failures.exception[0], MultipleFailuresError) {
      failures.size() == 4
      failures.every { it instanceof IOException }
    }
    result.testsSkippedCount == 0
    featureCounter.get() == 4
  }

  def "@Retry mode #mode executes setup and cleanup #expectedCount times"(String mode, int expectedCount) {
    given:
    setupCounter.set(0)
    cleanupCounter.set(0)

    when:
    def result = runner.runSpecBody("""
def setup() {
  setupCounter.incrementAndGet()
}

@Retry(mode = Retry.Mode.${mode})
def bar() {
  featureCounter.incrementAndGet()
  expect:
  throw new IOException()
}

def cleanup() {
  cleanupCounter.incrementAndGet()
}
    """)

    then:
    result.testsStartedCount == 1
    result.testsSucceededCount == 0
    result.testsFailedCount == 1
    with(result.failures.exception[0], MultipleFailuresError) {
      failures.size() == 4
      failures.every { it instanceof IOException }
    }
    result.testsSkippedCount == 0
    setupCounter.get() == expectedCount
    cleanupCounter.get() == expectedCount
    featureCounter.get() == 4

    where:
    mode                                    || expectedCount
    Retry.Mode.ITERATION.name()             || 1
    Retry.Mode.SETUP_FEATURE_CLEANUP.name() || 4
  }

  def "@Retry count can be changed"() {
    when:
    def result = runner.runSpecBody("""
@Retry(count = 5)
def bar() {
  featureCounter.incrementAndGet()
  expect:
  throw new Exception()
}
    """)

    then:
    result.testsStartedCount == 1
    result.testsSucceededCount == 0
    result.testsFailedCount == 1
    result.testsSkippedCount == 0
    featureCounter.get() == 6
  }


  def "@Retry rethrows non handled exceptions"() {
    given:
    runner.throwFailure = true

    when:
    def result = runner.runSpecBody("""
@Retry(exceptions=[IndexOutOfBoundsException])
def bar() {
  expect:
  throw new IllegalArgumentException()
}
    """)

    then:
    thrown(IllegalArgumentException)
  }

  def "@Retry rethrows non handled exceptions for data driven features"() {
    given:
    runner.throwFailure = true

    when:
    def result = runner.runSpecBody("""
@Retry(exceptions=[IndexOutOfBoundsException])
def bar() {
  expect:
  throw new IllegalArgumentException()

  where:
  ignore << [1, 2]
}
    """)

    then:
    thrown(IllegalArgumentException)
  }

  def "@Retry works for data driven features"() {
    when:
    def result = runner.runSpecBody("""
@Retry
def bar() {
  featureCounter.incrementAndGet()
  expect: test

  where:
  test << [false, false]
}
    """)

    then:
    result.testsSucceededCount == 1
    result.testsFailedCount == 2
    result.testsSkippedCount == 0
    featureCounter.get() == 8
  }

  def "@Retry for @Unroll'ed data driven feature"() {
    when:
    def result = runner.runSpecBody("""
@Retry
def bar() {
  featureCounter.incrementAndGet()
  expect: test

  where:
  test << [false, true, true]
}
    """)

    then:
    result.testsSucceededCount == 3
    result.testsFailedCount == 1
    result.testsSkippedCount == 0
    featureCounter.get() == 4 + 2
  }

  def "@Retry doesn't affect data driven feature where all iterations pass"() {
    when:
    def result = runner.runSpecBody("""
@Retry
def bar() {
  expect: test

  where:
  test << [true, true, true]
}
    """)

    then:
    result.testsSucceededCount == 4
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
  }

  def "@Retry doesn't affect @Unroll'ed data driven feature where all iterations pass"() {
    when:
    def result = runner.runSpecBody("""
@Retry
def bar() {
  expect: test

  where:
  test << [true, true, true]
}
    """)

    then:
    result.testsSucceededCount == 4
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
  }

  def "@Retry mode SETUP_FEATURE_CLEANUP ignores previous failures if a retry succeeds"() {
    when:
    def result = runner.runSpecBody("""
static int counter
@Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
def bar() {
  expect: counter++ > 0
}
    """)

    then:
    result.testsSucceededCount == 1
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
  }

  def "@Retry can add delay between iteration executions"() {
    when:
    long start = System.currentTimeMillis()
    def result = runner.runSpecBody("""
@Retry(delay = 100)
def bar() {
  featureCounter.incrementAndGet()
  expect: test

  where:
  test << [false, true, true]
}
    """)

    then:
    def duration = System.currentTimeMillis() - start
    duration > 300
    duration < 1000
    result.testsStartedCount == 4
    result.testsSucceededCount == 3
    result.testsFailedCount == 1
    result.testsSkippedCount == 0
    featureCounter.get() == 4 + 2
  }

  def "@Retry evaluates condition"() {
    when:
    def result = runner.runSpecBody("""
@Retry(condition = { failure.message.contains('1') })
def "bar #message"() {
  featureCounter.incrementAndGet()
  expect:
  assert false : message

  where:
  message << ['1', '2', '3']
}
    """)

    then:
    result.testsStartedCount == 4
    result.testsSucceededCount == 1
    result.testsFailedCount == 3
    featureCounter.get() == 4 + 1 + 1
  }

  def "@Retry does not evaluate condition if exception type is unexpected"() {
    when:
    def result = runner.runSpecBody("""
@Retry(exceptions = IllegalArgumentException, condition = { failure.message.contains('1') })
def "bar #exceptionClass #message"() {
  featureCounter.incrementAndGet()
  expect:
  throw exceptionClass.newInstance(message as String)

  where:
  exceptionClass           | message
  IllegalArgumentException | 1
  IllegalArgumentException | 2
  IllegalStateException    | 1
  IllegalStateException    | 2
  RuntimeException         | 1
  RuntimeException         | 2
}
    """)

    then:
    result.testsStartedCount == 7
    result.testsSucceededCount == 1
    result.testsFailedCount == 6
    featureCounter.get() == (4 + 1) + (1 + 1) + (1 + 1)
  }

  def "@Retry provides condition access to Specification instance"() {
    when:
    def result = runner.runSpecBody("""
int value
@Retry(condition = { instance.value == 2 })
def "bar #input"() {
  featureCounter.incrementAndGet()
  value = input

  expect:
  false

  where:
  input << [1, 2, 3]
}
    """)

    then:
    result.testsStartedCount == 4
    result.testsSucceededCount == 1
    result.testsFailedCount == 3
    featureCounter.get() == 1 + 4 + 1
  }

  def "@Retry can be declared on a spec class"() {
    when:
    def result = runner.runWithImports("""
@Retry
class Foo extends Specification {
  def foo() {
    featureCounter.incrementAndGet()
    expect:
    false
  }
  def bar() {
    featureCounter.incrementAndGet()
    expect:
    true
  }
  @Retry(count = 1)
  def baz() {
    featureCounter.incrementAndGet()
    expect:
    false
  }
}
    """)

    then:
    result.testsStartedCount == 3
    result.testsSucceededCount == 1
    result.testsFailedCount == 2
    featureCounter.get() == 4 + 1 + 2
  }

  def "@Retry declared on a spec class is inherited"() {
    when:
    def result = runner.runWithImports("""
@Retry(count = 1)
abstract class Foo extends Specification {
}
class Bar extends Foo {
  def bar() {
    featureCounter.incrementAndGet()
    expect:
    false
  }
}
    """)

    then:
    result.testsStartedCount == 1
    result.testsSucceededCount == 0
    result.testsFailedCount == 1
    featureCounter.get() == 2
  }

  def "@Retry declared on a subclass is applied to all features"() {
    when:
    def result = runner.runWithImports("""
abstract class Foo extends Specification {
  def foo() {
    featureCounter.incrementAndGet()
    expect:
    false
  }
}
@Retry(count = 1)
class Bar extends Foo {
  def bar() {
    featureCounter.incrementAndGet()
    expect:
    false
  }
}
    """)

    then:
    result.testsStartedCount == 2
    result.testsSucceededCount == 0
    result.testsFailedCount == 2
    featureCounter.get() == 2 + 2
  }

  def "@Retry declared on a spec class can be overridden"() {
    when:
    def result = runner.runWithImports("""
@Retry(count = 1)
abstract class Foo extends Specification {
  def foo() {
    featureCounter.incrementAndGet()
    expect:
    false
  }
}

@Retry(count = 2)
class Bar extends Foo {
  def bar() {
    featureCounter.incrementAndGet()
    expect:
    false
  }
}
    """)

    then:
    result.testsStartedCount == 2
    result.testsSucceededCount == 0
    result.testsFailedCount == 2
    featureCounter.get() == 3 + 3
  }

}
