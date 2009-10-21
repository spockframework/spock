package grails.plugin.spock.build

import org.codehaus.groovy.grails.test.FormattedOutput

import org.junit.runner.Result
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener

import junit.framework.AssertionFailedError

class GrailsSpeckRunListener extends RunListener {

    final protected out
    final protected failureCount

    Class speck
    List<FormattedOutput> speckFormattedOutputs
    
    GrailsSpeckRunListener(OutputStream out) {
        this.out = out
    }
    
    void testAssumptionFailure(Failure failure)  {

    }

    void testFailure(Failure failure)  {
        if (++failureCount == 1) out.println()
        out.println("                    ${failure.description.methodName}...FAILED")
        
        def description = failure.description
        def exception = failure.exception
        
        speckFormattedOutputs.each {
            if (exception instanceof AssertionFailedError) {
                it.formatter.addFailure(new TestCaseAdapter(description), exception)
            } else {
                it.formatter.addError(new TestCaseAdapter(description), exception)
            }
        }
    }

    void testFinished(Description description) {
        speckFormattedOutputs.each {
           it.formatter.endTest(new TestCaseAdapter(description))
        }
    }

    void testIgnored(Description description) {
 
    }

    void testRunFinished(Result result) {
        if (failureCount == 0) out.println("PASSED")
    }

    void testRunStarted(Description description) {
        failureCount = 0
        out.print("Running test ${speck.name}...")
    }

    void testStarted(Description description) {
        System.out.println("--Output from ${description.methodName}--")
        System.err.println("--Output from ${description.methodName}--")
        
         speckFormattedOutputs.each {
            it.formatter.startTest(new TestCaseAdapter(description))
         }
    }
}