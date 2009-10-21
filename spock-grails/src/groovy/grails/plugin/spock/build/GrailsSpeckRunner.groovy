package grails.plugin.spock.build

import spock.lang.Sputnik
import org.junit.runner.JUnitCore

import org.codehaus.groovy.grails.test.FormattedOutput
import org.codehaus.groovy.grails.test.XMLFormatter
import org.codehaus.groovy.grails.test.PlainFormatter

import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest

class GrailsSpeckRunner {
    
    protected savedOut
    protected savedErr;
    protected out
    protected err
    protected formattedOutputs

    protected reportsDir
    protected formats

    GrailsSpeckRunner(File reportsDir, List<String> formats) {
        this.reportsDir = reportsDir
        this.formats = formats
    }
    
    def runTests(suite) {
        
        def results = new JUnitResultsWrapper()
        def listener = new GrailsSpeckRunListener(System.out)
        def junit = new JUnitCore()
        junit.addListener(listener)

        suite.specks.each { speck ->
            reset()
            def junitTest = new JUnitTest(speck.name)
            try {
                replaceStandardStreams()
                prepareReports(speck)
                formattedOutputs.each { it.start(junitTest) }

                listener.setSpeck(speck, formattedOutputs) {
                    def start = System.currentTimeMillis()
                    def result = junit.run(speck)
                    junitTest.runTime = System.currentTimeMillis() - start
                    junitTest.setCounts(result.runCount, result.failureCount, 0)

                    results << result
                }
            } catch (Throwable t) {
                t.printStackTrace()
            } finally {
                formattedOutputs.each { output ->
                    output.end(junitTest, out.toString(), err.toString())
                }
                
                restoreStandardStreams()
            }
        }

        results
    }

    protected void reset() {
        this.formattedOutputs = null
        this.savedOut = null
        this.savedErr = null
        this.out = null
        this.err = null
    }
    
    protected void prepareReports(Class speck) {
        formattedOutputs = formats.collect { createFormatter(it, speck) }
    }

    protected FormattedOutput createFormatter(String type, Class speck) {
        if (type.equals("xml")) {
            new FormattedOutput(
                new File(reportsDir, "TEST-${speck.name}.xml"),
                new XMLFormatter()
            )
        } else if (type.equals("plain")) {
            new FormattedOutput(
                new File(reportsDir, "plain/TEST-${speck.name}.txt"),
                new PlainFormatter()
            )
        } else {
            throw new RuntimeException("Unknown formatter type: $type")
        }
    }

    protected void replaceStandardStreams() {
        this.savedOut = System.out
        this.savedErr = System.err

        this.out = new ByteArrayOutputStream()
        this.err = new ByteArrayOutputStream()

        System.setOut(new PrintStream(this.out))
        System.setErr(new PrintStream(this.err))
    }

    protected void restoreStandardStreams() {
        if (this.savedOut != null) System.out = this.savedOut
        if (this.savedErr != null) System.err = this.savedErr
    }
}