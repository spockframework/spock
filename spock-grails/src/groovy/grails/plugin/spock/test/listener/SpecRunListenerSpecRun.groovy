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

import junit.framework.Test 
import junit.framework.JUnit4TestCaseFacadeFactory

class SpecRunListenerSpecRun {
    final name
    final protected eventPublisher
    final protected reports
    final protected outAndErrSwapper
    final protected junitTest

    protected startTime
    def runCount = 0
    def failureCount = 0
    def errorCount = 0

    SpecRunListenerSpecRun(name, eventPublisher, reports, outAndErrSwapper) {
      this.name = name
      this.eventPublisher = eventPublisher
      this.reports = reports
      this.outAndErrSwapper = outAndErrSwapper
      this.junitTest = new JUnitTest(name)
    }

  void start() {
      eventPublisher.testCaseStart(name)
      outAndErrSwapper.swapIn()
      reports.startTestSuite(junitTest)
      startTime = System.currentTimeMillis()
    }
    
    void finish() {
      junitTest.runTime = System.currentTimeMillis() - startTime
      junitTest.setCounts(runCount, failureCount, errorCount)
      def (out,err) = outAndErrSwapper.swapOut()*.toString()
      reports.systemOutput = out
      reports.systemError = err
      reports.endTestSuite(junitTest)
      eventPublisher.testCaseEnd(name)
    }
    
    void testStarted(Description description) {
      def testName = description.methodName
      eventPublisher.testStart(testName)
      runCount++
      [System.out, System.err]*.println("--Output from ${testName}--")
      reports.startTest(JUnit4TestCaseFacadeFactory.createFacade(description))
    }
    
    void testFailure(Failure failure) {
      def testName = failure.description.methodName
      def testCase = JUnit4TestCaseFacadeFactory.createFacade(failure.description)
      def exception = failure.exception

      if (exception instanceof AssertionError) {
        eventPublisher.testFailure(testName, exception)
        failureCount++
        reports.addFailure(testCase, exception)
      } else {
        eventPublisher.testFailure(testName, exception, true)
        errorCount++
        reports.addError(testCase, exception)
      }
    }
    
    void testFinished(Description description) {
      reports.endTest(JUnit4TestCaseFacadeFactory.createFacade(description))
      eventPublisher.testEnd(description.methodName)
    }
}