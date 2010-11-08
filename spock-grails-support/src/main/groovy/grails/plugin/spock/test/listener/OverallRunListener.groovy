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

import org.codehaus.groovy.grails.test.io.SystemOutAndErrSwapper
import org.codehaus.groovy.grails.test.event.GrailsTestEventPublisher
import org.codehaus.groovy.grails.test.report.junit.JUnitReportsFactory

import org.junit.runner.Description
import org.junit.runner.Result
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener

class OverallRunListener extends RunListener {
  private final GrailsTestEventPublisher eventPublisher
  private final JUnitReportsFactory reportsFactory
  private final SystemOutAndErrSwapper outAndErrSwapper
  private final GrailsSpecTestTypeResult result
  
  private PerSpecRunListener perSpecListener

  OverallRunListener(GrailsTestEventPublisher eventPublisher, JUnitReportsFactory reportsFactory, SystemOutAndErrSwapper outAndErrSwapper, GrailsSpecTestTypeResult result) {
    this.eventPublisher = eventPublisher
    this.reportsFactory = reportsFactory
    this.outAndErrSwapper = outAndErrSwapper
    this.result = result
  }

  void testRunStarted(Description description) {
    // nothing to do
  }

  void testStarted(Description description) {
    getPerSpecRunListener(description).testStarted(description)
  }

  void testFailure(Failure failure) {
    getPerSpecRunListener(failure.description).testFailure(failure)
  }

  void testAssumptionFailure(Failure failure) {
    // assumptions (and AssumptionViolatedException) are specific to JUnit,
    // and are treated as ordinary failures
    getPerSpecRunListener(failure.description).testFailure(failure)
  }

  void testFinished(Description description) {
    getPerSpecRunListener(description).testFinished(description)
  }

  void testRunFinished(Result result) {
    // I can't think of a situation where this would be called without
    // a perSpecListener being available, even if it does happen
    // our handling options are very limited at this point in execution.
    getPerSpecRunListener()?.finish()
  }

  void testIgnored(Description description) {
    // nothing to do
  }
  
  private getPerSpecRunListener(description = null) {
    if (description && perSpecListener?.name != description.className) {
      perSpecListener?.finish()

      def specName = description.className
      perSpecListener = new PerSpecRunListener(specName, eventPublisher,
          reportsFactory.createReports(specName), outAndErrSwapper, result)

      perSpecListener.start()
    }

    perSpecListener
  }
}

