package grails.plugin.spock.build.test.adapter

import org.junit.runner.Result

class ResultsAdapter {

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