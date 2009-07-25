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

import org.junit.runner.RunWith

import spock.lang.*
import static spock.lang.Predef.*
import org.spockframework.smoke.EmbeddedSpeckRunner
import org.junit.runner.notification.RunListener
import org.spockframework.runtime.SpeckExecutionException

/**
 * @author Peter Niederwieser
 */
@Speck
@RunWith(Sputnik)
class FeatureUnrolling {
  def runner = new EmbeddedSpeckRunner()

  def "iterations of an unrolled feature count as separate tests"() {
    when:
    def result = runner.runSpeckBody("""
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
    runner.runSpeckBody """
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
    runner.runSpeckBody """
$annotation
def foo() {
  expect: true

  where:
  x << []
}
    """

    then:
    SpeckExecutionException e = thrown()

    where:
    annotation << ["", "@Unroll"]
  }

  def "if creation of a data provider fails, feature isn't unrolled"() {
    runner.throwFailure = false
    RunListener listener = Mock()
    runner.listeners << listener

    when:
    def result = runner.runSpeckBody("""
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
    // would have been invisible if it had not failed (cf. Speck JUnitTestResult)
    result.runCount == 0
    result.failureCount == 1
    result.ignoreCount == 0
  }
}