package org.spockframework.docs.extension

import spock.lang.PendingFeature
import spock.lang.PendingFeatureIf
import spock.lang.Specification

class PendingFeatureDocSpec extends Specification {
// tag::example-a[]
  @PendingFeature
  def "not implemented yet"() {
// end::example-a[]
    expect:
    false
  }

// tag::example-b[]
  @PendingFeature(exceptions = [
    UnsupportedOperationException,
    IllegalArgumentException
  ])
  def "I throw one of two exceptions"() {
// end::example-b[]
    expect:
    throw new IllegalArgumentException()
  }

// tag::example-c[]
  @PendingFeature(
    exceptions = UnsupportedOperationException,
    reason = 'operation not yet supported')
  @PendingFeature(
    exceptions = IllegalArgumentException,
    reason = 'arguments are broken')
  @PendingFeatureIf(
    value = { os.windows },
    reason = 'Does not yet work on Windows')
  def "I have various problems in certain situations"() {
// end::example-c[]
    expect:
    throw new IllegalArgumentException()
  }
}
