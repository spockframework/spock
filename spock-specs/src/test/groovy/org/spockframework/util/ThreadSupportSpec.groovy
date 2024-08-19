package org.spockframework.util

import spock.lang.IgnoreIf
import spock.lang.Requires
import spock.lang.Specification

class ThreadSupportSpec extends Specification {

  @Requires({ jvm.java21Compatible })
  def "creates virtual threads if available"() {
    when:
    def thread = ThreadSupport.virtualThreadIfSupported("test", {  })

    then:
    thread.virtual
    thread.class != Thread
    thread.name == "test"
  }

  @IgnoreIf({ jvm.java21Compatible })
  def "creates regular threads if not available"() {
    when:
    def thread = ThreadSupport.virtualThreadIfSupported("test", {  })

    then:
    thread.class == Thread
    thread.name == "test"
  }
}
