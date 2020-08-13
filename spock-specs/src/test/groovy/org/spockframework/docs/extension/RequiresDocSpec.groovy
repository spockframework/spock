package org.spockframework.docs.extension

import spock.lang.Requires
import spock.lang.Specification

class RequiresDocSpec extends Specification {
// tag::example-a[]
  @Requires({ os.windows })
  def "I'll only run on Windows"() {
// end::example-a[]
    expect:
    true
  }

// tag::example-b[]
  @Requires({ os.windows })
  @Requires({ jvm.java8 })
  def "I'll run only on Windows with Java 8"() {
// end::example-b[]
    expect:
    true
  }
}
