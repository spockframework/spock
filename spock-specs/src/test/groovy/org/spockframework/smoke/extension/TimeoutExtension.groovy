/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.extension

import org.spockframework.runtime.IStandardStreamsListener
import org.spockframework.runtime.SpockTimeoutError
import org.spockframework.runtime.extension.builtin.ThreadDumpUtilityType
import spock.lang.*
import spock.timeout.BaseTimeoutExtensionSpecification
import spock.util.environment.RestoreSystemProperties

import java.nio.file.Path

import static java.util.concurrent.TimeUnit.MILLISECONDS
import static org.spockframework.runtime.model.parallel.ExecutionMode.SAME_THREAD

/**
 * @author Peter Niederwieser
 */
@Isolated("The timings are quite tight and it can get flaky on weak machines if run in parallel, and thread dumps interfere with other test output.")
@Retry
class TimeoutExtension extends BaseTimeoutExtensionSpecification {
  @Shared
  Thread testFrameworkThread = Thread.currentThread()

  @TempDir
  Path tempDir

  @Timeout(1)
  def "method that completes in time"() {
    setup:
    Thread.sleep 500
  }

  @FailsWith(SpockTimeoutError)
  @Timeout(1)
  def "method that doesn't complete in time"() {
    setup:
    Thread.sleep 1100
  }

  @Timeout(value = 500, unit = MILLISECONDS)
  def "method that completes in time (millis)"() {
    setup:
    Thread.sleep 250
  }

  @FailsWith(SpockTimeoutError)
  @Timeout(value = 250, unit = MILLISECONDS)
  def "method that doesn't complete in time (millis)"() {
    setup:
    Thread.sleep 500
  }

  @Issue("https://github.com/spockframework/spock/issues/352")
  def "stack trace shows where thread is hung"() {
    when:
    runner.runSpecBody """
      @Timeout(value = 250, unit = MILLISECONDS)
      def foo() {
        setup: helper()
      }

      def helper() {
        Thread.sleep 300
      }
    """

    then:
    SpockTimeoutError e = thrown()
    stackTraceLooksLike e, """
      apackage.ASpec|helper|7
      apackage.ASpec|foo|3
    """
  }

  def "annotating spec class has same effect as annotating every feature method not already annotated with @Timeout"() {
    runner.throwFailure = false

    when:
    def result = runner.runWithImports("""
      @Timeout(value = 250, unit = MILLISECONDS)
      class Foo extends Specification {
        def foo() {
          expect: true
        }
        def bar() {
          setup:
          Thread.sleep 300
        }
        @Timeout(value = 100, unit = MILLISECONDS)
        def baz() {
          setup:
          Thread.sleep 150
        }
      }
    """)

    then:
    result.failures.size() == 2

    def e1 = result.failures[0].exception
    e1 instanceof SpockTimeoutError
    e1.timeout == 0.25

    def e2 = result.failures[1].exception
    e2 instanceof SpockTimeoutError
    e2.timeout == 0.1
  }

  @Issue("https://github.com/spockframework/spock/issues/303")
  @Timeout(1)
  def "method invocation occurs on regular test framework thread"() {
    expect:
    Thread.currentThread() == testFrameworkThread
  }

  def "SpockTimeoutError indicates timeout settings"() {
    when:
    runner.runSpecBody """
      @Timeout(value = 100, unit = MILLISECONDS)
      def foo() {
        setup: Thread.sleep 250
      }
    """

    then:
    SpockTimeoutError e = thrown()
    e.timeout == 0.1
  }

  def "repeatedly interrupts timed out method until it returns"() {
    when:
    runSpecWithInterrupts(3)

    then: 'timeout is propagated'
    thrown SpockTimeoutError

    and: 'thread dumps are captured on each interrupt attempt'
    assertThreadDumpsCaptured(2, 0, false)
  }

  def "can capture thread dumps on interrupt attempts"() {
    given:
    runner.configurationScript {
      timeout {
        printThreadDumpsOnInterruptAttempts true
      }
    }

    when:
    runSpecWithInterrupts(3)

    then:
    thrown SpockTimeoutError
    assertThreadDumpsCaptured(2, 2, false)
  }

  def "can set the maximum number of interrupt attempts with thread dump captured"() {
    given:
    runner.configurationScript {
      timeout {
        printThreadDumpsOnInterruptAttempts true
        maxInterruptAttemptsWithThreadDumps 1
      }
    }

    when:
    runSpecWithInterrupts(3)

    then:
    thrown SpockTimeoutError
    assertThreadDumpsCaptured(2, 1, true)
  }

  def "can capture thread dumps using jstack"() {
    given:
    runner.configurationScript {
      timeout {
        printThreadDumpsOnInterruptAttempts true
        threadDumpUtilityType ThreadDumpUtilityType.JSTACK
      }
    }

    when:
    runSpecWithInterrupts(3)

    then:
    thrown SpockTimeoutError
    assertThreadDumpsCaptured(2, 2, false, ThreadDumpUtilityType.JSTACK)
  }

  def "notifies custom interrupt listeners"() {
    given:
    List<Runnable> someListeners = [Mock(Runnable), Mock(Runnable)]
    Runnable anotherListener = Mock()
    runner.configurationScript {
      timeout {
        printThreadDumpsOnInterruptAttempts true
        maxInterruptAttemptsWithThreadDumps 1
        interruptAttemptListeners.addAll(someListeners)
        interruptAttemptListeners.add(anotherListener)
        interruptAttemptListeners.add({ println("inline interrupt listener") })
      }
    }

    when:
    runSpecWithInterrupts(3)

    then:
    thrown SpockTimeoutError
    assertThreadDumpsCaptured(2, 1, true)

    and:
    someListeners.each {
      2 * it._
    }
    2 * anotherListener._
    outputListener.count('inline interrupt listener') == 2
  }

  def "excludes dump-capturing thread from thread dumps"() {
    given:
    runner.configurationScript {
      timeout {
        printThreadDumpsOnInterruptAttempts true
      }
    }

    when:
    runSpecWithInterrupts(2)

    then:
    thrown SpockTimeoutError
    outputListener.count("Thread dump of current JVM") == 1
    outputListener.count(/"[spock.lang.Timeout] Watcher for method 'foo'" #/) == 0
  }

  @RestoreSystemProperties
  def "does not fail if thread dump capturing util is not available"() {
    given:
    System.setProperty('java.home', tempDir.toString())

    and:
    runner.configurationScript {
      timeout {
        printThreadDumpsOnInterruptAttempts true
      }
    }

    when:
    runSpecWithInterrupts(2)

    then:
    thrown SpockTimeoutError
    with(outputListener) {
      count("Could not find requested thread dump capturing utility") == 1
      count("Thread dump of current JVM") == 0
    }
  }

  @RestoreSystemProperties
  def "does not fail if java home is not set"() {
    given:
    System.clearProperty('java.home')

    and:
    runner.configurationScript {
      timeout {
        printThreadDumpsOnInterruptAttempts true
      }
    }

    when:
    runSpecWithInterrupts(2)

    then:
    thrown SpockTimeoutError
    with(outputListener) {
      count("Could not determine java home directory") == 1
      count("Thread dump of current JVM") == 0
    }
  }

  @Timeout(1)
  def "watcher thread has descriptive name"() {
    def group = Thread.currentThread().threadGroup
    def threads = new Thread[group.activeCount()]
    group.enumerate(threads)

    expect:
    threads.find { it.name == "[spock.lang.Timeout] Watcher for method 'watcher thread has descriptive name'" }
  }
}
