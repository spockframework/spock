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

package grails.plugin.spock.build.test.run

import org.codehaus.groovy.grails.test.FormattedOutput

import org.junit.runner.Result
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener

import junit.framework.AssertionFailedError

import grails.plugin.spock.build.test.adapter.TestCaseAdapter

class SpeckRunListener extends RunListener {
  final protected out
  final protected failureCount

  final protected speck
  final protected formattedOutputs

  SpeckRunListener(OutputStream out) {
    this.out = out
  }

  void setSpeck(Class speck, List<FormattedOutput> formattedOutputs, Closure duration) {
    this.speck = speck
    this.formattedOutputs = formattedOutputs
    duration()
    this.speck = null
    this.formattedOutputs = null
  }

  void testRunStarted(Description description) {
    failureCount = 0
    out.print("Running test ${speck.name}...")
  }

  void testStarted(Description description) {
    System.out.println("--Output from ${description.methodName}--")
    System.err.println("--Output from ${description.methodName}--")

    def testCase = new TestCaseAdapter(description)
    formattedOutputs.each {
      it.formatter.startTest(testCase)
    }
  }

  void testFailure(Failure failure) {
    if (++failureCount == 1) out.println()
    out.println("${failure.description.methodName}...FAILED")

    def testCase = new TestCaseAdapter(failure.description)
    def exception = failure.exception

    formattedOutputs.each {
      if (exception instanceof AssertionFailedError) {
        it.formatter.addFailure(testCase, exception)
      } else {
        it.formatter.addError(testCase, exception)
      }
    }
  }

  void testFinished(Description description) {
    def testCase = new TestCaseAdapter(description)
    formattedOutputs.each {
      it.formatter.endTest(testCase)
    }
  }

  void testRunFinished(Result result) {
    if (failureCount == 0) out.println("PASSED")
  }

  void testAssumptionFailure(Failure failure) {}

  void testIgnored(Description description) {}
}