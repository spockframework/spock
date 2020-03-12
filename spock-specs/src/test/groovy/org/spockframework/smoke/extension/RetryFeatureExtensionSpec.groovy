package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.SpockMultipleFailuresError
import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.extension.builtin.RetryIterationInterceptor
import org.spockframework.runtime.model.FeatureInfo
import spock.lang.*

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.concurrent.atomic.AtomicInteger

import org.opentest4j.MultipleFailuresError

import static org.junit.platform.testkit.engine.EventConditions.event
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure
import static org.junit.platform.testkit.engine.EventConditions.test
import static org.spockframework.runtime.model.parallel.ExecutionMode.SAME_THREAD

@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(RetryFeatureExtensionSpec.ChangeThreadExtension)
@interface ChangeThread {
}

@Execution(value = SAME_THREAD, reason = "tests use static field")
class RetryFeatureExtensionSpec extends EmbeddedSpecification {

  static AtomicInteger setupCounter = new AtomicInteger()
  static AtomicInteger cleanupCounter = new AtomicInteger()
  static AtomicInteger featureCounter = new AtomicInteger()
  static AtomicInteger extensionCounter = new AtomicInteger()
  static StringBuffer iterationBuffer

  def setup() {
    runner.addClassMemberImport(getClass())
    runner.throwFailure = false
    setupCounter.set(0)
    cleanupCounter.set(0)
    featureCounter.set(0)
    extensionCounter.set(0)
    iterationBuffer = new StringBuffer()
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

  def "@Retry mode #mode executes setup and cleanup #expectedCount times (parallel: #parallel)"(String mode, int expectedCount) {
    given:
    withParallelExecution(parallel)

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
    mode                                    || expectedCount || parallel
    Retry.Mode.ITERATION.name()             || 1             || false
    Retry.Mode.SETUP_FEATURE_CLEANUP.name() || 4             || false
    Retry.Mode.ITERATION.name()             || 1             || true
    Retry.Mode.SETUP_FEATURE_CLEANUP.name() || 4             || true
  }

  def "@Retry mode SETUP_FEATURE_CLEANUP is safe against offloading iteration execution to a different thread"() {
    given:
    runner.throwFailure = true
    runner.addClassImport(ChangeThread)

    when:
    runner.runSpecBody("""
@Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
@ChangeThread
def foo() {
  expect: false
}
    """)

    then:
    MultipleFailuresError e = thrown()
    e.failures.size() == 4
    e.failures.collect { it.getClass() } =~ [ConditionNotSatisfiedError]
  }

  def "@Retry mode SETUP_FEATURE_CLEANUP is safe against offloading iteration execution to a different thread in data driven feature"() {
    given:
    runner.addClassImport(ChangeThread)

    when:
    def result = runner.runSpecBody("""
@Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
@ChangeThread
def foo() {
  expect: false
  where: i << (1..2)
}
    """)

    then:
    result.failures.size() == 2
    result.failures.collect { it.exception.getClass() } =~ [SpockMultipleFailuresError]
    result.failures.collect { it.exception.failures.size() } =~ [4]
    result.failures.collectMany { it.exception.failures*.getClass() } =~ [ConditionNotSatisfiedError]
  }

  def "@Retry mode #mode executes setup and cleanup #expectedCount times for @Unroll'ed feature (parallel: #parallel)"(String mode, int expectedCount) {
    given:
    withParallelExecution(parallel)

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
  where:
  foo << [1, 2, 3]
}

def cleanup() {
  cleanupCounter.incrementAndGet()
}
    """)

    then:
    result.testsStartedCount == 4
    result.testsSucceededCount == 1
    result.testsFailedCount == 3
    with(result.failures.exception[0], MultipleFailuresError) {
      failures.size() == 4
      failures.every { it instanceof IOException }
    }
    result.testsSkippedCount == 0
    setupCounter.get() == expectedCount
    cleanupCounter.get() == expectedCount
    featureCounter.get() == 12

    where:
    mode                                    || expectedCount || parallel
    Retry.Mode.ITERATION.name()             || 3             || false
    Retry.Mode.SETUP_FEATURE_CLEANUP.name() || 12            || false
    Retry.Mode.ITERATION.name()             || 3             || true
    Retry.Mode.SETUP_FEATURE_CLEANUP.name() || 12            || true
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
@Retry(exceptions=[IndexOutOfBoundsException], mode = Retry.Mode.${mode})
def bar() {
  expect:
  throw new IllegalArgumentException()

  where:
  ignore << [1, 2]
}
    """)

    then:
    thrown(IllegalArgumentException)

    where:
    mode << [Retry.Mode.ITERATION.name(), Retry.Mode.SETUP_FEATURE_CLEANUP.name()]
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

  def "@Retry provides condition access to Specification instance shared fields"() {
    when:
    def result = runner.runSpecBody("""
@Shared
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

  def "@Retry provides condition access to Specification instance fields"() {
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

  def "@Retry provides condition access to static Specification fields"() {
    when:
    def result = runner.runSpecBody("""
static int value
@Retry(condition = { value == 2 })
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

  def "@Retry mode SETUP_FEATURE_CLEANUP runs remaining iterations after a failed one for @Unroll'ed features"() {
    when:
    def result = runner.runSpecBody("""
static int counter
@Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
def bar() {
  iterationBuffer.append(iteration)
  expect:
  if (iteration == 2) {
    throw new RuntimeException()
  }
  true
  where:
  iteration << [1, 2, 3, 4]
}
    """)

    then:
    result.testsSucceededCount == 4
    result.testsFailedCount == 1
    result.testsSkippedCount == 0
    iterationBuffer.toString() == "1222234"
  }

  def "@Retry mode SETUP_FEATURE_CLEANUP runs all iterations for @Unroll'ed features"() {
    when:
    def result = runner.runSpecBody("""
static int counter
@Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
def bar() {
  iterationBuffer.append(iteration)
  expect:
  false
  where:
  iteration << [1, 2, 3]
}
    """)

    then:
    result.testsSucceededCount == 1
    result.testsFailedCount == 3
    result.testsSkippedCount == 0
    iterationBuffer.toString() == "111122223333"
  }

  def "@Retry mode SETUP_FEATURE_CLEANUP correctly reports failed iterations for @Unroll'ed features"() {
    when:
    def result = runner.runSpecBody("""
@Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
def bar() {
  expect:
  result
  where:
  result << [true, false]
}
    """)

    then:
    result.testsSucceededCount == 2
    result.testsFailedCount == 1
    result.testsSkippedCount == 0
    result.testEvents().finished().assertEventsMatchLoosely(
      event(test("iteration:0"), finishedSuccessfully()),
      event(test("iteration:1"), finishedWithFailure())
    )
  }

  def "@Retry interceptor chains to enclosed interceptors each time"() {
    when:
    def result = runner.runWithImports("""
import spock.lang.Retry
import org.spockframework.smoke.extension.CountExecution

class Foo extends Specification {
  @Retry
  @CountExecution
  def bar(baz) {
    expect: false
  }
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
    extensionCounter.get() == 4
  }

  private def withParallelExecution(boolean enableParallelExecution) {
    runner.configurationScript {
      runner {
        parallel {
          enabled enableParallelExecution
          fixed(4)
        }
      }
    }
  }

  static class ChangeThreadExtension implements IAnnotationDrivenExtension<ChangeThread> {
    @Override
    void visitFeatureAnnotation(ChangeThread annotation, FeatureInfo feature) {
      assert feature.iterationInterceptors.any { it instanceof RetryIterationInterceptor }: "RetryIterationInterceptor must be added first"
      feature.addIterationInterceptor { invocation ->
        new Thread({ invocation.proceed() }).tap {
          it.start()
          it.join()
        }
      }
    }
  }

  static class CountExecutionExtension implements IAnnotationDrivenExtension<CountExecution> {
    @Override
    void visitFeatureAnnotation(CountExecution annotation, FeatureInfo feature) {
      feature.featureMethod.addInterceptor { invocation ->
        org.spockframework.smoke.extension.RetryFeatureExtensionSpec.extensionCounter.incrementAndGet()
        invocation.resolveArgument(0, "BAZ")
        invocation.proceed()
      }
    }
  }
}

@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(RetryFeatureExtensionSpec.CountExecutionExtension)
@interface CountExecution {
}
