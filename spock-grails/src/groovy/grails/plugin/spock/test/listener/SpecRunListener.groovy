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

import org.codehaus.groovy.grails.test.io.SystemOutAndErrSwapper
import org.codehaus.groovy.grails.test.event.GrailsTestEventPublisher
import org.codehaus.groovy.grails.test.report.junit.JUnitReportsFactory

import org.junit.runner.Description
import org.junit.runner.Result
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener

import org.codehaus.groovy.grails.test.report.junit.JUnitReportsFactory

class SpecRunListener extends RunListener {

  final protected GrailsTestEventPublisher eventPublisher
  final protected JUnitReportsFactory reportsFactory
  final protected SystemOutAndErrSwapper outAndErrSwapper

  protected currentSpecRun

  SpecRunListener(GrailsTestEventPublisher eventPublisher, JUnitReportsFactory reportsFactory, SystemOutAndErrSwapper outAndErrSwapper) {
    this.eventPublisher = eventPublisher
    this.reportsFactory = reportsFactory
    this.outAndErrSwapper = outAndErrSwapper
  }

  void testRunStarted(Description description) {}

  void testStarted(Description description) {
    if (currentSpecRun?.name != description.className) {
      currentSpecRun?.finish()

      def specName = description.className
      currentSpecRun = new SpecRunListenerSpecRun(specName, eventPublisher, reportsFactory.createReports(specName), outAndErrSwapper)
      currentSpecRun.start()
    }

    currentSpecRun.testStarted(description)
  }

  void testFailure(Failure failure) {
    currentSpecRun.testFailure(failure)
  }

  void testAssumptionFailure(Failure failure) {
    // assumptions (and AssumptionViolatedException) are specific to JUnit,
    // so we treat this as a failure
    currentSpecRun.testFailure(failure)
  }

  void testFinished(Description description) {
    currentSpecRun.testFinished(description)
  }

  void testRunFinished(Result result) {
    currentSpecRun.finish()
  }

  void testIgnored(Description description) {}
}

