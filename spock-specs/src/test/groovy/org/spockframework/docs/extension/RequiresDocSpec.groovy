package org.spockframework.docs.extension

import org.spockframework.runtime.extension.builtin.PreconditionContext
import spock.lang.Requires
import spock.lang.Specification

class RequiresDocSpec extends Specification {
// tag::example-a[]
  @Requires({
    // end::example-a[]
    PreconditionContext pc -> pc.
    // tag::example-a[]
    os.windows
  })
  def "I'll only run on Windows"() {
// end::example-a[]
    expect:
    true
  }

// tag::example-b[]
  @Requires({
    // end::example-b[]
    PreconditionContext pc -> pc.
    // tag::example-b[]
    os.windows
  })
  @Requires({
    // end::example-b[]
    PreconditionContext pc -> pc.
    // tag::example-b[]
    jvm.java8
  })
  def "I'll run only on Windows with Java 8"() {
// end::example-b[]
    expect:
    true
  }
}
