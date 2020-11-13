package org.spockframework.docs.extension

import spock.lang.Issue
import spock.lang.Specification

// tag::example[]
@Issue("http://my.issues.org/FOO-1")
class IssueDocSpec extends Specification {
  @Issue("http://my.issues.org/FOO-2")
  def "Foo should do bar"() {
    expect: true
  }

  @Issue(["http://my.issues.org/FOO-3", "http://my.issues.org/FOO-4"])
  def "I have two related issues"() {
    expect: true
  }

  @Issue(["http://my.issues.org/FOO-5", "http://my.issues.org/FOO-6"])
  @Issue("http://my.issues.org/FOO-7")
  def "I have three related issues"() {
    expect: true
  }
}
// end::example[]
