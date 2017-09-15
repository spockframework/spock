package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.ConditionNotSatisfiedError

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

  def "@Retry mode FEATURE for data driven"() {
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
    result.runCount == 12
    result.failureCount == 4
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

}
