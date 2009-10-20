package grails.plugin.spock.build

import org.junit.runner.Result

class JUnitResultsWrapper {

    final protected results = []
    
    def leftShift(Result result) {
        results << result
    }
    
    int errorCount() {
        0
    }
    
    int failureCount() {
        results.sum(0) { it.failureCount }
    }
    
    int runCount() {
        results.sum(0) { it.runCount }
    }
}