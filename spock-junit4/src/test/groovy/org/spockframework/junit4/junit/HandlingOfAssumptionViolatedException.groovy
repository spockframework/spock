/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.junit4.junit

import org.junit.Assume
import org.junit.runner.notification.RunListener

class HandlingOfAssumptionViolatedException extends JUnitBaseSpec {
  RunListener listener = Mock()

  def setup() {
    runner.listeners << listener
    runner.addClassMemberImport(Assume)
  }

  def "reported in regular feature method"() {
    when:
    runner.runSpecBody """
def foo() {
  setup:
  assumeTrue(false)
}
    """

    then:
    1 * listener.testStarted(_)

    then:
    1 * listener.testAssumptionFailure(_)

    then:
    1 * listener.testFinished(_)

    0 * listener.testFailure(_)
  }

  def "reported in data-driven unrolled feature method"() {
    when:
    runner.runSpecBody """
@Unroll
def foo() {
  setup:
  assumeTrue(false)

  where:
  i << (1..2)
}
    """

    then:
    2 * listener.testStarted(_)
    2 * listener.testAssumptionFailure(_)
    2 * listener.testFinished(_)
    0 * listener.testFailure(_)
  }

  def "ignored in data-driven feature method that isn't unrolled"() {
    when:
    runner.runSpecBody """
def foo() {
  setup:
  assumeTrue(false)

  where:
  i << (1..2)
}
    """

    then:
    1 * listener.testStarted(_)
    0 * listener.testAssumptionFailure(_)
    1 * listener.testFinished(_)
    0 * listener.testFailure(_)
  }

  def "reported in setup"() {
    when:
    runner.runSpecBody """
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
    2 * listener.testStarted(_)
    2 * listener.testAssumptionFailure(_)
    2 * listener.testFinished(_)
    0 * listener.testFailure(_)
  }

  def "reported in @Before"() {
    when:
    runner.runSpecBody """
@org.junit.Before
void before() {
  assumeTrue(false)
}

def foo() {
  expect: true
}

def bar() {
  expect: true
}
    """

    then:
    2 * listener.testStarted(_)
    2 * listener.testAssumptionFailure(_)
    2 * listener.testFinished(_)
    0 * listener.testFailure(_)
  }
}
