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

import grails.plugin.spock.test.GrailsSpecTestTypeResult

import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.codehaus.groovy.grails.test.event.GrailsTestEventPublisher
import org.codehaus.groovy.grails.test.report.junit.JUnitReports
import org.codehaus.groovy.grails.test.io.SystemOutAndErrSwapper

import junit.framework.AssertionFailedError
import junit.framework.JUnit4TestCaseFacadeFactory
import junit.framework.JUnit4TestCaseFacade

class PerSpecRunListener {
  final String name
  
  private final GrailsTestEventPublisher eventPublisher
  private final JUnitReports reports
  private final SystemOutAndErrSwapper outAndErrSwapper
  private final GrailsSpecTestTypeResult result
  private final JUnitTest testSuite

  private long startTime

  // number of tests that have run
  private int runCount = 0

  // number of tests that have failed; note that number of failures reported to
  // eventPublisher/reports may be higher, for example when both feature method
  // and cleanup method fail
  private int failureCount = 0

  // number of tests that have erred; note that number of errors reported to
  // eventPublisher/reports may be higher, for example when both feature method
  // and cleanup method erred
  private int errorCount = 0

  // tells whether testFailure() has been called for the current test
  private boolean testFailed = false

  // tells whether the setupSpec() method has failed
  private boolean setupSpecFailed = false

  private final Map<Description, JUnit4TestCaseFacade> testsByDescription = [:]

  PerSpecRunListener(String name, GrailsTestEventPublisher eventPublisher, JUnitReports reports,
      SystemOutAndErrSwapper outAndErrSwapper, GrailsSpecTestTypeResult result) {
    this.name = name
    this.eventPublisher = eventPublisher
    this.reports = reports
    this.outAndErrSwapper = outAndErrSwapper
    this.result = result

    testSuite = new JUnitTest(name)
  }

  void start() {
    eventPublisher.testCaseStart(name)
    reports.startTestSuite(testSuite)

    outAndErrSwapper.swapIn()
    startTime = System.currentTimeMillis()
  }

  void finish() {
    result.runCount += runCount
    result.failCount += failureCount + errorCount
    
    testSuite.runTime = System.currentTimeMillis() - startTime
    testSuite.setCounts(runCount, failureCount, errorCount)

    def (out, err) = outAndErrSwapper.swapOut()*.toString()
    reports.systemOutput = out
    reports.systemError = err

    eventPublisher.testCaseEnd(name)
    reports.endTestSuite(testSuite)
  }

  void testStarted(Description description) {
    def testName = description.methodName

    eventPublisher.testStart(testName)
    reports.startTest(getTest(description))

    [System.out, System.err]*.println("--Output from ${testName}--")
    testFailed = false
    runCount++
  }

  void testFailure(Failure failure) {
    def testName = failure.description.methodName

    if (testName == null) {
      // in the following we assume that setupSpec() can only fail once
      if (runCount == 0 && !setupSpecFailed) {
        testName = "setupSpec"
        setupSpecFailed = true
      } else {
        testName = "cleanupSpec"
      }

      // prevent increase of failureCount/errorCount because the error doesn't stem from a "test"
      testFailed = true
    }

    def testCase = getTest(failure.description)
    def exception = failure.exception

    if (exception instanceof AssertionError) {
      eventPublisher.testFailure(testName, exception)
      reports.addFailure(testCase, toAssertionFailedError(exception))
      if (!testFailed) failureCount++
    } else {
      eventPublisher.testFailure(testName, exception, true)
      reports.addError(testCase, exception)
      if (!testFailed) errorCount++
    }

    testFailed = true
  }

  void testFinished(Description description) {
    reports.endTest(getTest(description))
    eventPublisher.testEnd(description.methodName)
    testFailed = false
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
}