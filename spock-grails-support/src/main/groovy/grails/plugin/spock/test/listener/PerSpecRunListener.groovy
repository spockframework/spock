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

package grails.plugin.spock.test.listener

import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest
import org.junit.runner.Description
import org.junit.runner.notification.Failure

import junit.framework.AssertionFailedError
import junit.framework.JUnit4TestCaseFacadeFactory

class PerSpecRunListener {
  final name
  
  private final eventPublisher
  private final reports
  private final outAndErrSwapper
  private final testSuite

  private startTime
  private runCount = 0
  private failureCount = 0
  private errorCount = 0

  private testsByDescription = [:]

  PerSpecRunListener(name, eventPublisher, reports, outAndErrSwapper) {
    this.name = name
    this.eventPublisher = eventPublisher
    this.reports = reports
    this.outAndErrSwapper = outAndErrSwapper
    this.testSuite = new JUnitTest(name)
  }

  void start() {
    eventPublisher.testCaseStart(name)
    outAndErrSwapper.swapIn()
    reports.startTestSuite(testSuite)
    startTime = System.currentTimeMillis()
  }

  void finish() {
    testSuite.runTime = System.currentTimeMillis() - startTime
    testSuite.setCounts(runCount, failureCount, errorCount)
    def (out, err) = outAndErrSwapper.swapOut()*.toString()
    reports.systemOutput = out
    reports.systemError = err
    reports.endTestSuite(testSuite)
    eventPublisher.testCaseEnd(name)
  }

  void testStarted(Description description) {
    def testName = description.methodName
    eventPublisher.testStart(testName)
    runCount++
    [System.out, System.err]*.println("--Output from ${testName}--")
    reports.startTest(getTest(description))
  }

  void testFailure(Failure failure) {
    def testName = failure.description.methodName
    
    // If the failure is in setupSpec or cleanupSpec the description
    // is for the class object, with no associated methodName.
    // So we interpret this especially.
    if (testName == null) {
      // Spock will not run a spec with no features, therefore
      // we are guaranteed to have at least one test run/failed
      // if this failure did come from cleanupSpec
      testName = noTestsHaveRun ? "setupSpec" : "cleanupSpec"
    }
    
    def testCase = getTest(failure.description)
    def exception = failure.exception

    if (exception instanceof AssertionError) {
      eventPublisher.testFailure(testName, exception)
      failureCount++
      reports.addFailure(testCase, toAssertionFailedError(exception))
    } else {
      eventPublisher.testFailure(testName, exception, true)
      errorCount++
      reports.addError(testCase, exception)
    }
  }

  void testFinished(Description description) {
    reports.endTest(getTest(description))
    eventPublisher.testEnd(description.methodName)
  }

  // JUnitReports requires us to always pass the same Test instance
  // for a test, so we cache it; this scheme also works for the case
  // where testFailure() is invoked without a prior call to testStarted() 
  private getTest(description) {
    def test = testsByDescription.get(description)
    if (test == null) {
      test = JUnit4TestCaseFacadeFactory.createFacade(description)
      testsByDescription.put(description, test)
    }
    test
  }

  private toAssertionFailedError(assertionError) {
    def result = new AssertionFailedError(assertionError.toString())
    result.stackTrace = assertionError.stackTrace
    result
  }
  
  private boolean isNoTestsHaveRun() {
    runCount == 0 && failureCount == 0 && errorCount == 0
  }
}