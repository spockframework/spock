package grails.plugin.spock.build.test

import spock.lang.Sputnik
import org.junit.runner.JUnitCore


import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest

class GrailsSpeckRunner {
    
    protected reportFactory

    GrailsSpeckRunner(File reportsDir, List<String> formats) {
        reportFactory = new ReportFactory(reportsDir, formats)
    }
    
    def runTests(suite) {
        
        def outAndErrSwapper = new SystemOutAndErrSwapper()
        def results = new JUnitResultsWrapper()
        def listener = new GrailsSpeckRunListener(System.out)
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