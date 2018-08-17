package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.ConditionNotSatisfiedError
import spock.lang.*

import java.util.concurrent.atomic.AtomicInteger

class RetryFeatureExtensionSpec extends EmbeddedSpecification {

  def setup() {
    runner.throwFailure = false
  }

  def "@Retry fails after retries are exhausted"() {
    when:
    def result = runner.runWithImports("""
import spock.lang.Retry

class Foo extends Specification {
  @Retry
  def bar() {
    expect: false
  }
}
    """)

    then:
    result.runCount == 1
    result.failureCount == 4
    result.failures.exception.every { it instanceof ConditionNotSatisfiedError }
    result.ignoreCount == 0
  }

  def "@Retry works for normal exceptions"() {
    when:
    def result = runner.runWithImports("""
import spock.lang.Retry

class Foo extends Specification {
  @Retry
  def bar() {
    expect:
    throw new IOException()
  }
}
    """)

    then:
    result.runCount == 1
    result.failureCount == 4
    result.failures.exception.every { it instanceof IOException }
    result.ignoreCount == 0
  }

  static AtomicInteger setupCounter = new AtomicInteger()
  static AtomicInteger cleanupCounter = new AtomicInteger()

  @Unroll
  def "@Retry mode #mode executes setup and cleanup #expectedCount times"(String mode, int expectedCount) {
    given:
    setupCounter.set(0)
    cleanupCounter.set(0)

    when:
    def result = runner.runWithImports("""
import spock.lang.Retry

class Foo extends Specification {
  def setup() {
    org.spockframework.smoke.extension.RetryFeatureExtensionSpec.setupCounter.incrementAndGet()
  }

  @Retry(mode = Retry.Mode.${mode})
  def bar() {
    expect:
    throw new IOException()
  }

  def cleanup() {
    org.spockframework.smoke.extension.RetryFeatureExtensionSpec.cleanupCounter.incrementAndGet()
  }
}
    """)

    then:
    result.runCount == 1
    result.failureCount == 4
    result.failures.exception.every { it instanceof IOException }
    result.ignoreCount == 0
    setupCounter.get() == expectedCount
    cleanupCounter.get() == expectedCount

    where:
    mode                                    || expectedCount
    Retry.Mode.FEATURE.name()               || 1
    Retry.Mode.ITERATION.name()             || 1
    Retry.Mode.SETUP_FEATURE_CLEANUP.name() || 4
  }

  def "@Retry count can be changed"() {
    when:
    def result = runner.runWithImports("""import spock.lang.Retry

class Foo extends Specification {
  @Retry(count = 5)
  def bar() {
    expect:
    throw new Exception()
  }
}
    """)

    then:
    result.runCount == 1
    result.failureCount == 6
    result.ignoreCount == 0
  }


  def "@Retry rethrows non handled exceptions"() {
    given:
    runner.throwFailure = true

    when:
    def result = runner.runWithImports("""
import spock.lang.Retry

class Foo extends Specification {
  @Retry(exceptions=[IndexOutOfBoundsException])
  def bar() {
    expect:
    throw new IllegalArgumentException()
  }
}
    """)

    then:
    thrown(IllegalArgumentException)
  }

  def "@Retry rethrows non handled exceptions for data driven features"() {
    given:
    runner.throwFailure = true

    when:
    def result = runner.runWithImports("""
import spock.lang.Retry

class Foo extends Specification {
  @Retry(exceptions=[IndexOutOfBoundsException])
  def bar() {
    expect:
    throw new IllegalArgumentException()
    
    where:
    ignore << [1, 2]
  }
}
    """)

    then:
    thrown(IllegalArgumentException)
  }

  def "@Retry rethrows non handled exceptions for data driven features with FEATURE mode"() {
    given:
    runner.throwFailure = true

    when:
    def result = runner.runWithImports("""
import spock.lang.Retry

class Foo extends Specification {
  @Retry(exceptions=[IndexOutOfBoundsException], mode = Retry.Mode.FEATURE)
  def bar() {
    expect:
    throw new IllegalArgumentException()
    
    where:
    ignore << [1, 2]
  }
}
    """)

    then:
    thrown(IllegalArgumentException)
  }

  def "@Retry works for data driven features"() {
    when:
    def result = runner.runWithImports("""
import spock.lang.Retry

class Foo extends Specification {
  @Retry
  def bar() {
    expect: test

    where:
    test << [false, false]
  }
}
    """)

    then:
    result.runCount == 1
    result.failureCount == 8
    result.ignoreCount == 0
  }

  def "@Retry for @Unroll'ed data driven feature"() {
    when:
    def result = runner.runWithImports("""
import spock.lang.Retry

class Foo extends Specification {
  @Unroll
  @Retry
  def bar() {
    expect: test

    where:
    test << [false, true, true]
  }
}
    """)

    then:
    result.runCount == 3
    result.failureCount == 4
    result.ignoreCount == 0
  }

  def "@Retry mode FEATURE retries complete feature when an iteration fails"() {
    when:
    def result = runner.runWithImports("""
import spock.lang.Retry

class Foo extends Specification {
  @Unroll
  @Retry(mode=Retry.Mode.FEATURE)
  def bar() {
    expect: test

    where:
    test << [true, true, false]
  }
}
    """)

    then:
    result.runCount == 4 * 3
    result.failureCount == 4
    result.ignoreCount == 0
  }

  def "@Retry mode FEATURE stops retries after all iterations have passed once"() {
    when:
    def result = runner.runWithImports("""
import spock.lang.Retry

class Foo extends Specification {
  static int[] counters = new int[3]
  @Unroll
  @Retry(mode=Retry.Mode.FEATURE)
  def bar() {
    counters[index]++

    expect:
    index < 2 || counters[index] > 1

    where:
    index << [0, 1, 2]
  }
}
    """)

    then:
    result.runCount == 2 * 3
    result.failureCount == 0
    result.ignoreCount == 0
  }

  def "@Retry doesn't affect data driven feature where all iterations pass"() {
    when:
    def result = runner.runWithImports("""
import spock.lang.Retry

class Foo extends Specification {
  @Retry
  def bar() {
    expect: test

    where:
    test << [true, true, true]
  }
}
    """)

    then:
    result.runCount == 1
    result.failureCount == 0
    result.ignoreCount == 0
  }

  def "@Retry doesn't affect data driven feature where all iterations pass in mode FEATURE"() {
    when:
    def result = runner.runWithImports("""
import spock.lang.Retry

class Foo extends Specification {
  @Retry(mode = Retry.Mode.FEATURE)
  def bar() {
    expect: test

    where:
    test << [true, true, true]
  }
}
    """)

    then:
    result.runCount == 1
    result.failureCount == 0
    result.ignoreCount == 0
  }

  def "@Retry doesn't affect @Unroll'ed data driven feature where all iterations pass"() {
    when:
    def result = runner.runWithImports("""
import spock.lang.Retry

class Foo extends Specification {
  @Unroll
  @Retry
  def bar() {
    expect: test

    where:
    test << [true, true, true]
  }
}
    """)

    then:
    result.runCount == 3
    result.failureCount == 0
    result.ignoreCount == 0
  }

  def "@Retry mode SETUP_FEATURE_CLEANUP ignores previous failures if a retry succeeds"() {
    when:
    def result = runner.runWithImports("""
import spock.lang.Retry

class Foo extends Specification {
  static int counter
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  def bar() {
    expect: counter++ > 0
  }
}
    """)

    then:
    result.runCount == 1
    result.failureCount == 0
    result.ignoreCount == 0
  }

  def "@Retry can add delay between executions"() {
    when:
    long start = System.currentTimeMillis()
    def result = runner.runWithImports("""
import spock.lang.Retry

class Foo extends Specification {
  @Retry(delay = 100, mode = Retry.Mode.FEATURE)
  def bar() {
    expect: test

    where:
    test << [false, false, false, false, false, false, false, false, true]
  }
}
    """)

    then:
    def duration = System.currentTimeMillis() - start
    duration > 300
    duration < 1000
    result.runCount == 1
    result.failureCount == 32
    result.ignoreCount == 0
  }

  def "@Retry can add delay between iteration executions"() {
    when:
    long start = System.currentTimeMillis()
    def result = runner.runWithImports("""
import spock.lang.Retry

class Foo extends Specification {
  @Retry(delay = 100)
  def bar() {
    expect: test

    where:
    test << [false, true, true]
  }
}
    """)

    then:
    def duration = System.currentTimeMillis() - start
    duration > 300
    duration < 1000
    result.runCount == 1
    result.failureCount == 4
    result.ignoreCount == 0
  }

  def "@Retry evaluates condition"() {
    when:
    def result = runner.runWithImports("""
import spock.lang.Retry

class Foo extends Specification {
  @Unroll
  @Retry(condition = { failure.message.contains('1') })
  def "bar #message"() {
    expect:
    assert false : message

    where:
    message << ['1', '2', '3']
  }
}
    """)

    then:
    result.runCount == 3
    result.failureCount == 4 + 1 + 1
  }

  def "@Retry does not evaluate condition if exception type is unexpected"() {
    when:
    def result = runner.runWithImports("""
import spock.lang.Retry

class Foo extends Specification {
  @Unroll
  @Retry(exceptions = IllegalArgumentException, condition = { failure.message.contains('1') })
  def "bar #exceptionClass #message"() {
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
}
    """)

    then:
    result.runCount == 6
    result.failureCount == (4 + 1) + (1 + 1) + (1 + 1)
  }

  def "@Retry provides condition access to Specification instance"() {
    when:
    def result = runner.runWithImports("""
import spock.lang.Retry

class Foo extends Specification {
  int value
  @Unroll
  @Retry(condition = { instance.value == 2 })
  def "bar #input"() {
    value = input
    
    expect:
    false

    where:
    input << [1, 2, 3]
  }
}
    """)

    then:
    result.runCount == 3
    result.failureCount == 1 + 4 + 1
  }

  def "@Retry can be declared on a spec class"() {
    when:
    def result = runner.runWithImports("""
import spock.lang.Retry

@Retry
class Foo extends Specification {
  def foo() {
    expect:
    false
  }
  def bar() {
    expect:
    true
  }
  @Retry(count = 1)
  def baz() {
    expect:
    false
  }
}
    """)

    then:
    result.runCount == 3
    result.failureCount == 4 + 0 + 2
  }

  def "@Retry declared on a spec class is inherited"() {
    when:
    def result = runner.runWithImports("""
import spock.lang.Retry

@Retry(count = 1)
abstract class Foo extends Specification {
}
class Bar extends Foo {
  def bar() {
    expect:
    false
  }
}
    """)

    then:
    result.runCount == 1
    result.failureCount == 2
  }

  def "@Retry declared on a subclass is applied to all features"() {
    when:
    def result = runner.runWithImports("""
import spock.lang.Retry

abstract class Foo extends Specification {
  def foo() {
    expect:
    false
  }
}
@Retry(count = 1)
class Bar extends Foo {
  def bar() {
    expect:
    false
  }
}
    """)

    then:
    result.runCount == 2
    result.failureCount == 2 + 2
  }

  def "@Retry declared on a spec class can be overridden"() {
    when:
    def result = runner.runWithImports("""
import spock.lang.Retry

@Retry(count = 1)
abstract class Foo extends Specification {
  def foo() {
    expect:
    false
  }
}
@Retry(count = 2)
class Bar extends Foo {
  def bar() {
    expect:
    false
  }
}
    """)

    then:
    result.runCount == 2
    result.failureCount == 3 + 3
  }

}
