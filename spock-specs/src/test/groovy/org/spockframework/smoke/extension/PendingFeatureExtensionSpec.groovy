package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification

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
    result.runCount == 1
    result.failureCount == 0
    result.ignoreCount == 0
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
    result.runCount == 1
    result.failureCount == 0
    result.ignoreCount == 0
    // unfortunately we can't access the skip reason here, but at least the branch is coverd
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
    result.runCount == 1
    result.failureCount == 0
    result.ignoreCount == 0
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
    result.runCount == 1
    result.failureCount == 0
    result.ignoreCount == 0
  }

  def "@PendingFeature marks @Unroll'ed data driven feature where every iteration fails as skipped"() {
    when:
    def result = runner.runWithImports("""

class Foo extends Specification {
  @Unroll
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
    result.runCount == 3
    result.failureCount == 0
    result.ignoreCount == 0
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
    result.runCount == 1
    result.failureCount == 0
    result.ignoreCount == 0
  }


  def "@PendingFeature marks @Unroll'ed data driven feature where at least one iteration fails as skipped"() {
    when:
    def result = runner.runWithImports("""

class Foo extends Specification {
  @Unroll
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
    result.runCount == 3
    result.failureCount == 0
    result.ignoreCount == 0
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


  def "@PendingFeature marks @Unroll'ed data driven feature where all iterations pass as failed"() {
    when:
    runner.runWithImports("""

class Foo extends Specification {
  @Unroll
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
