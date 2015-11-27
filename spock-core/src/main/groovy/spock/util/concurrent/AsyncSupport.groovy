package spock.util.concurrent

import groovy.transform.CompileStatic

@CompileStatic
trait AsyncSupport {
    int getTimeout() {
        return 5
    }

    double getDelay() {
        return 0.5
    }

    void eventually(Closure predicate) {
        new PollingConditions(timeout: timeout, delay: delay).eventually {
            predicate()
        }
    }

    void eventuallyNot(Closure predicate) {
        try {
            new PollingConditions(timeout: timeout, delay: delay).eventually {
                predicate()
            }
            throw new PredicateEventuallyFulfilled()
        } catch (AssertionError _) {
            // Predicate not fulfilled
        }
    }
}

class PredicateEventuallyFulfilled extends RuntimeException {
}
