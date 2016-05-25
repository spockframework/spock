/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.extension

import java.util.concurrent.TimeUnit

import org.spockframework.runtime.SpockTimeoutError
import org.spockframework.EmbeddedSpecification

import spock.lang.*

import static java.util.concurrent.TimeUnit.*

/**
 * @author Peter Niederwieser
 */
class TimeoutExtension extends EmbeddedSpecification {

  def setup() {
    runner.addClassMemberImport TimeUnit
  }

  @Timeout(10)
  def "method that completes in time"() {
    setup: Thread.sleep 500
  }

  @FailsWith(SpockTimeoutError)
  @Timeout(1)
  def "method that doesn't complete in time"() {
    setup: Thread.sleep 12000
  }

  @Timeout(value = 5000, unit = MILLISECONDS)
  def "method that completes in time (millis)"() {
    setup: Thread.sleep 250
  }

  @FailsWith(SpockTimeoutError)
  @Timeout(value = 2500, unit = MILLISECONDS)
  def "method that doesn't complete in time (millis)"() {
    setup: Thread.sleep 5000
  }

  @Issue("http://issues.spockframework.org/detail?id=230")
  def "stack trace shows where thread is hung"() {
    when:
    runner.runSpecBody """
      @Timeout(value = 2500, unit = MILLISECONDS)
      def foo() {
        setup: helper()
      }

      def helper() {
        Thread.sleep 10000
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
      @Timeout(value = 2500, unit = MILLISECONDS)
      class Foo extends Specification {
        def foo() {
          expect: true
        }
        def bar() {
          setup:
          Thread.sleep 10000
        }
        @Timeout(value = 100, unit = MILLISECONDS)
        def baz() {
          setup:
          Thread.sleep 15000
        }
      }
    """)

    then:
    result.failures.size() == 2

    def e1 = result.failures[0].exception
    e1 instanceof SpockTimeoutError
    e1.timeout == 2.5

    def e2 = result.failures[1].exception
    e2 instanceof SpockTimeoutError
    e2.timeout == 0.1
  }

  def "SpockTimeoutError indicates timeout settings"() {
    when:
    runner.runSpecBody """
      @Timeout(value = 100, unit = MILLISECONDS)
      def foo() {
        setup: Thread.sleep 10000
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

// will not work with junit's Parallel Computer
class TimeoutExtensionThreadAffinity extends EmbeddedSpecification{

  @Shared Thread testFrameworkThread = Thread.currentThread()

  @Issue("issues.spockframework.org/detail?id=181")
  @Timeout(1)
  def "method invocation occurs on regular test framework thread"() {
    expect:
        Thread.currentThread() == testFrameworkThread
  }
}
