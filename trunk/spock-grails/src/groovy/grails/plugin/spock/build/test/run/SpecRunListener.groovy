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

import grails.plugin.spock.build.test.report.ReportFactory
import org.junit.runner.Description
import org.junit.runner.Result
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener

class SpecRunListener extends RunListener {
  final protected statusOut
  final protected reportFactory
  
  protected currentSpecRun

  SpecRunListener(File reportsDir, List<String> formats, PrintStream statusOut) {
    this.statusOut = statusOut
    this.reportFactory = new ReportFactory(reportsDir, formats)
  }

  void testRunStarted(Description description) {}

  void testStarted(Description description) {
    if (currentSpecRun?.name != description.className) {
      currentSpecRun?.finish()

      currentSpecRun = new SpecRunListenerSpecRun(description.className, reportFactory, statusOut)
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

