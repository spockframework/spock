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

import org.junit.runner.Result
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener

import grails.plugin.spock.build.test.report.ReportFactory

class SpeckRunListener extends RunListener {
  final protected statusOut
  final protected reportFactory
  
  final protected currentSpeckRun

  SpeckRunListener(File reportsDir, List<String> formats, PrintStream statusOut) {
    this.statusOut = statusOut
    this.reportFactory = new ReportFactory(reportsDir, formats)
  }

  void testRunStarted(Description description) {}

  void testStarted(Description description) {
    if (currentSpeckRun?.name != description.className) {
      currentSpeckRun?.finish()

      currentSpeckRun = new SpeckRunListenerSpeckRun(description.className, reportFactory, statusOut)
      currentSpeckRun.start()
    }

    currentSpeckRun.testStarted(description)
  }

  void testFailure(Failure failure) {
    currentSpeckRun.testFailure(failure)
  }

  void testFinished(Description description) {
    currentSpeckRun.testFinished(description)
  }

  void testRunFinished(Result result) {
    currentSpeckRun.finish()
  }

  void testAssumptionFailure(Failure failure) {}

  void testIgnored(Description description) {}
}

