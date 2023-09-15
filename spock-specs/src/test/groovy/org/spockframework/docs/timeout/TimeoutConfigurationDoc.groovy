package org.spockframework.docs.timeout

import org.spockframework.runtime.SpockTimeoutError
import org.spockframework.runtime.extension.builtin.ThreadDumpUtilityType
import spock.lang.Isolated
import spock.timeout.BaseTimeoutExtensionSpecification

@Isolated("The thread dump interferes with the parallel logging")
class TimeoutConfigurationDoc extends BaseTimeoutExtensionSpecification {

  def "thread dump capturing respects provided configuration"() {
    given:
    runner.configurationScript {
      // tag::example[]
      timeout {
        // boolean, default false
        printThreadDumpsOnInterruptAttempts true
        // integer, default 3
        maxInterruptAttemptsWithThreadDumps 1
        // org.spockframework.runtime.extension.builtin.ThreadDumpUtilityType, default JCMD
        threadDumpUtilityType ThreadDumpUtilityType.JSTACK
        // list of java.lang.Runnable, default []
        interruptAttemptListeners.add({ println('Unsuccessful interrupt occurred!') })
      }
      // end::example[]
    }

    when:
    runSpecWithInterrupts(3)

    then:
    thrown SpockTimeoutError
    assertThreadDumpsCaptured(2, 1, true, ThreadDumpUtilityType.JSTACK)
    outputListener.count('Unsuccessful interrupt occurred!') == 2
  }
}
