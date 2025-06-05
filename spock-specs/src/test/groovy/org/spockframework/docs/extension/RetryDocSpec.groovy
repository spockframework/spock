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
}

class FlakyIntegrationSpecA extends FlakyIntegrationSpec {
// tag::example-a[]
  @Retry
  def "retry 3 times"() {
    expect: true
  }

  @Retry(count = 5)
  def "retry 5 times"() {
    expect: true
  }

  @Retry(exceptions = [IOException])
  def "only retry on IOException"() {
    expect: true
  }

  @Retry(condition = { failure.message.contains('foo') })
  def "only retry if condition on failure holds"() {
    expect: true
  }

  @Retry(condition = { instance.field != null })
  def "only retry if condition on instance holds"() {
    expect: true
  }

  @Retry
  def "retry failing feature methods"() {
    expect: true

    where:
    data << sql.execute('')
  }

  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  def "retry with setup and cleanup"() {
    expect: true

    where:
    data << sql.execute('')
  }

  @Retry(delay = 1000)
  def "retry after 1000 ms delay"() {
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
