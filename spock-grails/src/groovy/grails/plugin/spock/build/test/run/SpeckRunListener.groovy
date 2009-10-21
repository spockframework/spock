package grails.plugin.spock.build.test.run

import org.codehaus.groovy.grails.test.FormattedOutput

import org.junit.runner.Result
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener

import junit.framework.AssertionFailedError

import grails.plugin.spock.build.test.adapter.TestCaseAdapter

class SpeckRunListener extends RunListener {

    final protected out
    final protected failureCount

    final protected speck
    final protected formattedOutputs
    
    SpeckRunListener(OutputStream out) {
        this.out = out
    }

    void setSpeck(Class speck, List<FormattedOutput> formattedOutputs, Closure duration) {
        this.speck = speck
        this.formattedOutputs = formattedOutputs
        duration()
        this.speck = null
        this.formattedOutputs = null
    }
    
    void testRunStarted(Description description) {
        failureCount = 0
        out.print("Running test ${speck.name}...")
    }

    void testStarted(Description description) {
        System.out.println("--Output from ${description.methodName}--")
        System.err.println("--Output from ${description.methodName}--")
        
         formattedOutputs.each {
            it.formatter.startTest(new TestCaseAdapter(description))
         }
    }
    
    void testFailure(Failure failure)  {
        if (++failureCount == 1) out.println()
        out.println("                    ${failure.description.methodName}...FAILED")
        
        def description = failure.description
        def exception = failure.exception
        
        formattedOutputs.each {
            if (exception instanceof AssertionFailedError) {
                it.formatter.addFailure(new TestCaseAdapter(description), exception)
            } else {
                it.formatter.addError(new TestCaseAdapter(description), exception)
            }
        }
    }

    void testFinished(Description description) {
        formattedOutputs.each {
           it.formatter.endTest(new TestCaseAdapter(description))
        }
    }

    void testRunFinished(Result result) {
        if (failureCount == 0) out.println("PASSED")
    }
    
    void testAssumptionFailure(Failure failure) {}
    void testIgnored(Description description) {}
}