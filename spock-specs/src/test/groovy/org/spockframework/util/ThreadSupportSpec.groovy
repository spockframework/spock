package org.spockframework.util

import org.spockframework.runtime.extension.builtin.PreconditionContext
import spock.lang.IgnoreIf
import spock.lang.Requires
import spock.lang.Specification
import spock.lang.SpockTypeChecked

import static groovy.transform.TypeCheckingMode.SKIP

class ThreadSupportSpec extends Specification {

  @SpockTypeChecked(SKIP)
  @Requires({ jvm.java21Compatible })
  def "creates virtual threads if available"() {
    when:
    def thread = ThreadSupport.virtualThreadIfSupported("test", {  })

    then:
    thread.virtual
    thread.class != Thread
    thread.name == "test"
  }

  @IgnoreIf({ PreconditionContext pc -> pc.jvm.java21Compatible })
  def "creates regular threads if not available"() {
    when:
    def thread = ThreadSupport.virtualThreadIfSupported("test", {  })

    then:
    thread.class == Thread
    thread.name == "test"
  }
}
