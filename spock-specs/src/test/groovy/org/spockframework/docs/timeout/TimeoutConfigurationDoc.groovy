package org.spockframework.docs.timeout

import org.spockframework.runtime.SpockTimeoutError
import org.spockframework.runtime.extension.builtin.ThreadDumpUtilityType
import spock.lang.Isolated
import spock.lang.SpockTypeChecked
import spock.timeout.BaseTimeoutExtensionSpecification

import static groovy.transform.TypeCheckingMode.SKIP

@Isolated("The thread dump interferes with the parallel logging")
class TimeoutConfigurationDoc extends BaseTimeoutExtensionSpecification {

  @SpockTypeChecked(SKIP)
  private initTimeoutSettings() {
    runner.configurationScript {
      // tag::example[]
      timeout {
        // java.time.Duration, default null: If set applies global timeout for all features, unless overridden
        globalTimeout java.time.Duration.ofMinutes(1);
        // boolean, default false: Determines whether the global timeout will be applied to fixtures
        applyGlobalTimeoutToFixtures false
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
  }

  def "thread dump capturing respects provided configuration"() {
    given:
    initTimeoutSettings()

    when:
    runSpecWithInterrupts(3)

    then:
    thrown SpockTimeoutError
    assertThreadDumpsCaptured(2, 1, true, ThreadDumpUtilityType.JSTACK)
    outputListener.count('Unsuccessful interrupt occurred!') == 2
  }
}
