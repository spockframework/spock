package org.spockframework.docs.extension

import groovy.sql.Sql
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification

abstract
// tag::example-common[]
class FlakyIntegrationSpec extends Specification {
// end::example-common[]
  @Shared
  def sql = Sql.newInstance("jdbc:h2:mem:", "org.h2.Driver")

// tag::example-d[]
  @Retry(exceptions = IllegalArgumentException, count = 2)
  @Retry(exceptions = IllegalAccessException, count = 4)
  def retryDependingOnException() {
// end::example-d[]
    expect: true
  }
}

class FlakyIntegrationSpecA extends FlakyIntegrationSpec {
// tag::example-a[]
  @Retry
  def retry3Times() {
    expect: true
  }

  @Retry(count = 5)
  def retry5Times() {
    expect: true
  }

  @Retry(exceptions = [IOException])
  def onlyRetryIOException() {
    expect: true
  }

  @Retry(condition = { failure.message.contains('foo') })
  def onlyRetryIfConditionOnFailureHolds() {
    expect: true
  }

  @Retry(condition = { instance.field != null })
  def onlyRetryIfConditionOnInstanceHolds() {
    expect: true
  }

  @Retry
  def retryFailingIterations() {
    expect: true

    where:
    data << sql.execute('')
  }

  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  def retryWholeFeature() {
    expect: true

    where:
    data << sql.execute('')
  }

  @Retry(delay = 1000)
  def retryAfter1000MsDelay() {
    expect: true
  }
}
// end::example-a[]

// tag::example-b1[]
@Retry
// end::example-b1[]
class FlakyIntegrationSpecB extends FlakyIntegrationSpec {
// tag::example-b2[]
  def "will be retried with config from class"() {
    expect: true
  }

  @Retry(count = 5)
  def "will be retried using its own config"() {
    expect: true
  }
}
// end::example-b2[]

// tag::example-c[]
@Retry(count = 1)
abstract class AbstractIntegrationSpec extends Specification {
  def inherited() {
    expect: true
  }
}

class FooIntegrationSpec extends AbstractIntegrationSpec {
  def foo() {
    expect: true
  }
}

@Retry(count = 2)
class BarIntegrationSpec extends AbstractIntegrationSpec {
  def bar() {
    expect: true
  }
}
// end::example-c[]
