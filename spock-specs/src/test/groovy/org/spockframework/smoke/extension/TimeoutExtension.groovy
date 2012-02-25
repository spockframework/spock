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

import spock.lang.*
import org.spockframework.EmbeddedSpecification
import org.junit.runner.Result

/**
 *
 * @author Peter Niederwieser
 */
class TimeoutExtension extends EmbeddedSpecification {
  @Timeout(1)
  def "method that completes in time"() {
    setup: Thread.sleep(500)
  }

  @FailsWith(SpockTimeoutError)
  @Timeout(1)
  def "method that doesn't complete in time"() {
    setup: Thread.sleep(1100)
  }

  @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
  def "method that completes in time (millis)"() {
    setup: Thread.sleep(250)
  }

  @FailsWith(SpockTimeoutError)
  @Timeout(value = 250, unit = TimeUnit.MILLISECONDS)
  def "method that doesn't complete in time (millis)"() {
    setup: Thread.sleep(300)
  }

  @Issue("http://issues.spockframework.org/detail?id=230")
  def "stack trace of error is the stack of the hung thread"() {
    when:
    runner.runSpecBody("""
      @Timeout(1)
      def "not in time"() {
        setup:
        Thread.sleep(1100)
      }
    """)

    then:
    def e = thrown(SpockTimeoutError)
    def frames = e.stackTrace.toList()
    frames.size() == 1
    def topFrame = frames.first()
    topFrame.className == "apackage.ASpec"
    topFrame.methodName == "not in time"
  }

  def "timeout on spec"() {
    runner.addClassImport(TimeUnit)
    runner.throwFailure = false

    when:
    def result = runner.runWithImports("""
      @Timeout(value = 250, unit = TimeUnit.MILLISECONDS)
      class Foo extends Specification {
        def "in time"() {
          expect: true
        }
        def "not in time"() {
          setup:
          Thread.sleep(300)
        }
        @Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
        def "not in time - overridden timeout"() {
          setup:
          Thread.sleep(150)
        }
      }
    """)

    then:
    result.failures.size() == 2

    def e1 = result.failures[0].exception
    e1 instanceof SpockTimeoutError
    e1.timeoutValue == 250

    def e2 = result.failures[1].exception
    e2 instanceof SpockTimeoutError
    e2.timeoutValue == 100
  }
}