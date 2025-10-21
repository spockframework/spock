package org.spockframework.smoke

import groovy.test.NotYetImplemented
import org.opentest4j.AssertionFailedError
import org.spockframework.runtime.GroovyRuntimeUtil
import spock.lang.FailsWith
import spock.lang.Issue
import spock.lang.Requires
import spock.lang.Specification

//Those tests requires Groovy 3.0.3+, see UsageOfNotYetImplementedJUnit4 for deprecated groovy.transform.NotYetImplemented tests
@Issue(["https://github.com/spockframework/spock/issues/1127", "https://issues.apache.org/jira/browse/GROOVY-9492"])
@Requires({ GroovyRuntimeUtil.MAJOR_VERSION >= 3 })
class UsageOfNotYetImplemented extends Specification {

  @NotYetImplemented
  def "expected to fail"() {
    expect: false
  }

  @NotYetImplemented
  def "allowed to raise arbitrary exception"() {
    setup:
    throw new IOException("ouch")
  }

  @FailsWith(AssertionError)
  @NotYetImplemented
  def "not allowed to pass"() {
    expect: true
  }

  @FailsWith(AssertionFailedError)
  @NotYetImplemented(exception = AssertionFailedError)
  def "not allowed to pass with custom exception type"() {
    expect: true
  }
}
