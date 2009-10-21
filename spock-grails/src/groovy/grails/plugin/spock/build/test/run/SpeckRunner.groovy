package grails.plugin.spock.build.test.run

import spock.lang.Sputnik
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