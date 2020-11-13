package org.spockframework.docs.extension

import org.spockframework.runtime.extension.builtin.PreconditionContext
import spock.lang.IgnoreIf
import spock.lang.Specification

class IgnoreIfDocSpec extends Specification {
// tag::example-a[]
  @IgnoreIf({ System.getProperty("os.name").toLowerCase().contains("windows") })
  def "I'll run everywhere but on Windows"() {
// end::example-a[]
    expect:
    true
  }

// tag::example-b[]
  @IgnoreIf({ os.windows })
  def "I will run everywhere but on Windows"() {
// end::example-b[]
    expect:
    true
  }

// tag::example-c[]
  @IgnoreIf({ os.windows })
  @IgnoreIf({ jvm.java8 })
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
}
