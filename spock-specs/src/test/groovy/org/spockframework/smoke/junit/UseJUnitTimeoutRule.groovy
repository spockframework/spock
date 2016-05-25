/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.junit

import org.spockframework.EmbeddedSpecification
import org.junit.runner.Result
import org.spockframework.runtime.SpockTimeoutError
import spock.lang.FailsWith
import spock.lang.Ignore

class UseJUnitTimeoutRule extends EmbeddedSpecification {
  ThreadLocal<Integer> timeout = new ThreadLocal<>()

  def setup() {
    runner.configurationScript = {
      runner {
        filterStackTrace = false
      }
    }
  }

  void cleanup() {
    timeout.remove()
  }

  def "feature method that completes in time"() {
    timeout.set(5000)

    when:
    runFeatureMethodThatSleeps(0)

    then:
    noExceptionThrown()
  }

  @Ignore("sometimes fails due to changed rule semantics in JUnit 4.10")
  def "feature method that does not complete in time"() {
    timeout.set(2500)

    when:
    runFeatureMethodThatSleeps(10000)

    then:
    Exception e = thrown()
    e.message.contains "timed out"
  }

  private Result runFeatureMethodThatSleeps(delay) {
    runner.runSpecBody """
@org.junit.Rule
org.junit.rules.Timeout timeout = new org.junit.rules.Timeout(${timeout.get()})

def foo() {
  setup:
  Thread.sleep($delay)
}
     """
  }
}
