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

import org.junit.runner.JUnitCore

import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest

import grails.plugin.spock.build.test.report.ReportFactory
import grails.plugin.spock.build.test.adapter.ResultsAdapter
import grails.plugin.spock.build.test.io.SystemOutAndErrSwapper

class SpeckRunner {
  protected reportFactory

  SpeckRunner(File reportsDir, List<String> formats) {
    reportFactory = new ReportFactory(reportsDir, formats)
  }

  def runTests(suite) {
    def outAndErrSwapper = new SystemOutAndErrSwapper()
    def results = new ResultsAdapter()
    def listener = new SpeckRunListener(System.out)
    def junit = new JUnitCore()
    junit.addListener(listener)

    suite.specks.each { speck ->

      def junitTest = new JUnitTest(speck.name)
      def reports = reportFactory.createReports(speck.name)


      outAndErrSwapper.swap { out, err ->
        try {
          reports*.start(junitTest)
          listener.setSpeck(speck, reports) {
            def start = System.currentTimeMillis()
            def result = junit.run(speck)
            junitTest.runTime = System.currentTimeMillis() - start
            junitTest.setCounts(result.runCount, result.failureCount, 0)

            results << result
          }
        } finally {
          reports*.end(junitTest, out.toString(), err.toString())
        }
      }
    }

    results
  }

}