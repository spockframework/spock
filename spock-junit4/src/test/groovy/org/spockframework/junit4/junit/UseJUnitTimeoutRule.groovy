/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.junit4.junit


import spock.lang.Ignore

class UseJUnitTimeoutRule extends JUnitBaseSpec {
  def timeout

  def setup() {
    runner.configurationScript = {
      runner {
        filterStackTrace = false
      }
    }
  }

  def "feature method that completes in time"() {
    timeout = 500

    when:
    runFeatureMethodThatSleeps(0)

    then:
    noExceptionThrown()
  }

  @Ignore("sometimes fails due to changed rule semantics in JUnit 4.10")
  def "feature method that does not complete in time"() {
    timeout = 250

    when:
    runFeatureMethodThatSleeps(500)

    then:
    Exception e = thrown()
    e.message.contains "timed out"
  }

  private def runFeatureMethodThatSleeps(delay) {
    runner.runSpecBody """
@org.junit.Rule
org.junit.rules.Timeout timeout = new org.junit.rules.Timeout($timeout)

def foo() {
  setup:
  Thread.sleep($delay)
}
     """
  }
}
