package org.spockframework.docs.extension

import org.opentest4j.TestAbortedException
import org.spockframework.runtime.extension.builtin.PreconditionContext
import spock.lang.PendingFeature
import spock.lang.PendingFeatureIf
import spock.lang.Specification

class PendingFeatureIfDocSpec extends Specification {
// tag::example-a[]
  @PendingFeatureIf({
    // end::example-a[]
    PreconditionContext pc -> pc.
    // tag::example-a[]
    os.windows
  })
  def "I'm not yet implemented on windows, but I am on other operating systems"() {
// end::example-a[]
    expect:
    throw new TestAbortedException()
  }

// tag::example-b[]
  @PendingFeatureIf({
    // end::example-b[]
    PreconditionContext pc -> pc.
    // tag::example-b[]
    sys.targetEnvironment == "prod"
  })
  def "This feature isn't deployed out to production yet, and isn't expected to pass"() {
// end::example-b[]
    expect:
    true
  }

// tag::example-c[]
  @PendingFeature(exceptions = UnsupportedOperationException)
  @PendingFeatureIf(
    exceptions = IllegalArgumentException,
    value = {
      // end::example-c[]
      PreconditionContext pc -> pc.
      // tag::example-c[]
      os.windows
    },
    reason = 'Does not yet work on Windows')
  @PendingFeatureIf(
    exceptions = IllegalAccessException,
    value = {
      // end::example-c[]
      PreconditionContext pc -> pc.
      // tag::example-c[]
      jvm.java8
    },
    reason = 'Does not yet work on Java 8')
  def "I have various problems in certain situations"() {
// end::example-c[]
    expect:
    throw new TestAbortedException()
  }
}
