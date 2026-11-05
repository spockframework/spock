package org.spockframework.docs.extension

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.extension.builtin.PreconditionContext
import spock.lang.IgnoreIf
import spock.lang.SpockTypeChecked

import static groovy.transform.TypeCheckingMode.SKIP

class IgnoreIfDocSpec extends EmbeddedSpecification {
// tag::example-a[]
  @IgnoreIf({ System.getProperty("os.name").toLowerCase().contains("windows") })
  def "I'll run everywhere but on Windows"() {
// end::example-a[]
    expect:
    true
  }

// tag::example-b[]
  @IgnoreIf({
    // end::example-b[]
    PreconditionContext pc -> pc.
    // tag::example-b[]
    os.windows
  })
  def "I will run everywhere but on Windows"() {
// end::example-b[]
    expect:
    true
  }

// tag::example-c[]
  @IgnoreIf({
    // end::example-c[]
    PreconditionContext pc -> pc.
    // tag::example-c[]
    os.windows
  })
  @IgnoreIf({
    // end::example-c[]
    PreconditionContext pc -> pc.
    // tag::example-c[]
    jvm.java8
  })
  def "I'll run everywhere but on Windows or anywhere on Java 8"() {
// end::example-c[]
    expect:
    true
  }

// tag::example-d[]
  @IgnoreIf({ PreconditionContext it -> it.os.windows })
  def "I will run everywhere but not on Windows"() {
// end::example-d[]
    expect:
    true
  }

  @SpockTypeChecked(SKIP)
// tag::example-e[]
  @IgnoreIf({ os.windows })
  @IgnoreIf({ data.a == 5 && data.b >= 6 })
  def "I'll run everywhere but on Windows and only if a != 5 and b < 6"(int a, int b) {
    // ...
// end::example-e[]
    expect:
    true

// tag::example-e[]
    where:
    [a, b] << [(1..10), (1..8)].combinations()
  }
// end::example-e[]

// tag::example-f[]
  @IgnoreIf(value = {
    // end::example-f[]
    PreconditionContext pc -> pc.
    // tag::example-f[]
    os.macOs
  }, reason = "No platform driver available")
  def "For the given reason, I will not run on MacOS"() {
// end::example-f[]
    expect:
    true
  }

  def "IgnoreIf can be configured to be inherited"() {
    when:
    def result = runner.runWithImports (
      /* tag::example-g[] */"""
@IgnoreIf(value = { jvm.java8Compatible }, inherited = true)
abstract class Foo extends Specification {
}

class Bar extends Foo {
  def "I won't run on Java 8 and above"() {
    expect: true
  }
}
""") // end::example-g[]

    then:
    result.testsStartedCount == 0
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
    result.testsAbortedCount == 0
    result.testsSucceededCount == 0
    result.containersSkippedCount == 1
  }
}
