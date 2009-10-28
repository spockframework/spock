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

package org.spockframework.smoke.parameterization

import org.junit.runner.notification.RunListener
import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.SpockExecutionException

/**
 * @author Peter Niederwieser
 */
class UnrolledFeatureMethods extends EmbeddedSpecification {
  def "iterations of an unrolled feature count as separate tests"() {
    when:
    def result = runner.runSpecBody("""
@Unroll
def foo() {
  expect: true

  where:
  x << [1, 2, 3]
}
    """)

    then:
    result.runCount == 3
    result.failureCount == 0
    result.ignoreCount == 0
  }

  def "iterations of an unrolled feature foo are named foo[0], foo[1], etc."() {
    RunListener listener = Mock()
    runner.listeners << listener

    when:
    runner.runSpecBody """
@Unroll
def foo() {
  expect: true

  where:
  x << [1, 2, 3]
}
    """

    then:
    interaction {
      def count = 0
      3 * listener.testStarted { it.methodName == "foo[${count++}]" }
    }
  }

  def "a feature with an empty data provider causes the same error regardless if it's unrolled or not"() {
    when:
    runner.runSpecBody """
$annotation
def foo() {
  expect: true

  where:
  x << []
}
    """

    then:
    SpockExecutionException e = thrown()

    where:
    annotation << ["", "@Unroll"]
  }

  def "if creation of a data provider fails, feature isn't unrolled"() {
    runner.throwFailure = false
    RunListener listener = Mock()
    runner.listeners << listener

    when:
    def result = runner.runSpecBody("""
@Unroll
def foo() {
  expect: true

  where:
  x << [1]
  y << { throw new Exception() }()
}
    """)

    then:
    1 * listener.testFailure { it.description.methodName == "foo" }

    // it's OK for runCount to be zero here; mental model: method "foo"
    // would have been invisible if it had not failed (cf. Spec JUnitTestResult)
    result.runCount == 0
    result.failureCount == 1
    result.ignoreCount == 0
  }

  def "naming pattern that refers to data variables"() {
    RunListener listener = Mock()
    runner.listeners << listener

    when:
    def result = runner.runSpecBody("""
@Unroll("one #y two #x three")
def foo() {
  expect: true

  where:
  x << [1, 2, 3]
  y << ["a", "b", "c"]
}
    """)

    then:
    1 * listener.testStarted { it.methodName == "one a two 1 three" }
    1 * listener.testStarted { it.methodName == "one b two 2 three" }
    1 * listener.testStarted { it.methodName == "one c two 3 three" }
    0 * listener.testStarted(_)
  }

    def "naming pattern that refers to feature name and iteration count"() {
    RunListener listener = Mock()
    runner.listeners << listener

    when:
    def result = runner.runSpecBody("""
@Unroll("one #featureName two #iterationCount three")
def foo() {
  expect: true

  where:
  x << [1, 2, 3]
  y << ["a", "b", "c"]
}
    """)

    then:
    1 * listener.testStarted { it.methodName == "one foo two 0 three" }
    1 * listener.testStarted { it.methodName == "one foo two 1 three" }
    1 * listener.testStarted { it.methodName == "one foo two 2 three" }
    0 * listener.testStarted(_)
  }
}