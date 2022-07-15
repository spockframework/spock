/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.junit4.junit

import org.junit.Assume

class HandlingOfAssumptionViolatedException extends JUnitBaseSpec {

  def setup() {
    runner.addClassMemberImport(Assume)
  }

  def "reported in regular feature method"() {
    when:
    def result = runner.runSpecBody """
def foo() {
  setup:
  assumeTrue(false)
}
    """

    then:
    result.testsStartedCount == 1
    result.testsAbortedCount == 1
    result.testsFailedCount == 0
  }

  def "reported in data-driven unrolled feature method"() {
    when:
    def result = runner.runFeatureBody """
setup:
assumeTrue(false)

where:
i << (1..2)
    """

    then:
    result.testsStartedCount == 3
    result.testsSucceededCount == 1
    result.testsAbortedCount == 2
    result.testsFailedCount == 0
  }

  def "reported in setup"() {
    given:
    runner.throwFailure = false

    when:
    def result = runner.runSpecBody """
def setup() {
  assumeTrue(false)
}

def foo() {
  expect: true
}

def bar() {
  expect: false
}
    """

    then:
    result.testsStartedCount == 2
    result.testsAbortedCount == 2
    result.testsFailedCount == 0
  }

  def "reported in @Before"() {
    given:
    runner.throwFailure = false

    when:
    def result = runner.runSpecBody """
@org.junit.Before
void before() {
  assumeTrue(false)
}

def foo() {
  expect: true
}

def bar() {
  expect: false
}
    """

    then:
    result.testsStartedCount == 2
    result.testsAbortedCount == 2
    result.testsFailedCount == 0
  }
}
