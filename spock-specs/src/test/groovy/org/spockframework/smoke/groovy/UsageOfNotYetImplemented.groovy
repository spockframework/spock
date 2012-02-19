package org.spockframework.smoke.groovy

import groovy.transform.NotYetImplemented

import junit.framework.AssertionFailedError

import spock.lang.Specification
import spock.lang.FailsWith

// make sure that Groovy's @NotYetImplemented works with Spock
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
  
  @FailsWith(AssertionFailedError)
  @NotYetImplemented
  def "not allowed to pass"() {
    expect: true
  }
}
