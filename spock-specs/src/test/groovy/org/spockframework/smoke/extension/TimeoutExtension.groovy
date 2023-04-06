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

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.SpockTimeoutError
import spock.lang.*

import java.util.concurrent.TimeUnit

import static java.util.concurrent.TimeUnit.MILLISECONDS
import static org.spockframework.runtime.model.parallel.ExecutionMode.SAME_THREAD
/**
 * @author Peter Niederwieser
 */
@Isolated("The timings are quite tight and it can get flaky on weak machines if run in parallel.")
@Execution(SAME_THREAD)
@Retry
class TimeoutExtension extends EmbeddedSpecification {
  @Shared Thread testFrameworkThread = Thread.currentThread()

  def setup() {
    runner.addClassMemberImport TimeUnit
  }

  @Timeout(1)
  def "method that completes in time"() {
    setup: Thread.sleep 500
  }

  @FailsWith(SpockTimeoutError)
  @Timeout(1)
  def "method that doesn't complete in time"() {
    setup: Thread.sleep 1100
  }

  @Timeout(value = 500, unit = MILLISECONDS)
  def "method that completes in time (millis)"() {
    setup: Thread.sleep 250
  }

  @FailsWith(SpockTimeoutError)
  @Timeout(value = 250, unit = MILLISECONDS)
  def "method that doesn't complete in time (millis)"() {
    setup: Thread.sleep 500
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
    runner.runSpecBody """
      @Timeout(value = 100, unit = MILLISECONDS)
      def foo() {
        when: Thread.sleep 99999999999
        then: thrown InterruptedException

        when: Thread.sleep 99999999999
        then: thrown InterruptedException

        when: Thread.sleep 99999999999
        then: thrown InterruptedException
      }
    """

    then:
    thrown SpockTimeoutError
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
