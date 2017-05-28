/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spock.util.concurrent

import groovy.transform.CompileStatic
import org.spockframework.lang.ConditionBlock

/**
 * Wrapper of {@link PollingConditions} which allows to use it as trait.
 *
 * Wrapper creates new {@link PollingConditions} per each call of {@link AsyncSupport#eventually} or {@link AsyncSupport#eventuallyNot} call.
 *
 * <p>Usage example:</p>
 *
 * <pre>
 * class MySpec extends Specification implements AsyncSupport {
 *    def 'my test'(){
 *      def machine = new Machine()
 *
 *      when:
 *      machine.start()
 *
 *      then:
 *      eventually {
 *          assert machine.temperature >= 100
 *          assert machine.efficiency >= 0.9
 *      }
 *    }
 * }
 * </pre>
 *
 * Polling conditions variables could be overridden by implementing own getters of its parameters.
 *
 * Example of override timeout and delay:
 *
 * <pre>
 * class MySpec extends Specification implements AsyncSupport {
 *    double timeout = 4
 *
 *    double getDelay(){
 *        return 0.5
 *    }
 *    def 'my test'(){
 *
 *      def machine = new Machine()
 *
 *      when:
 *      machine.start()
 *
 *      then:
 *      eventuallyNot {
 *          assert machine.temperature >= 10000
 *          assert machine.efficiency >= 1.0
 *      }
 *    }
 * }
 * </pre>
 */
@CompileStatic
trait AsyncSupport {

    /**
     * @return timeout for {@link PollingConditions}
     */
    double getTimeout() {
        return 1
    }

    /**
     * @return delay for {@link PollingConditions}
     */
    double getDelay() {
        return 0.1
    }

    /**
     * @return initial for {@link PollingConditions}
     */
    double getInitialDelay() {
        return 0
    }

    /**
     * @return factor for {@link PollingConditions}
     */
    double getFactor() {
        return 1
    }

    /**
     * Repeatedly evaluates the specified conditions until they are satisfied or the timeout has elapsed.
     *
     * @param conditions the conditions to evaluate
     *
     * @throws InterruptedException if evaluation is interrupted
     */
    @ConditionBlock
    void eventually(Closure predicate) {
        new PollingConditions(timeout: timeout, delay: delay, factor: factor, initialDelay: initialDelay).eventually {
            predicate()
        }
    }

    /**
     * Repeatedly evaluates the specified conditions and wait until the timeout. If conditions are satisfied then exception is thrown.
     *
     * @param conditions the conditions to evaluate
     *
     * @throws PredicateEventuallyFulfilled when conditions is satisfied
     */
    @ConditionBlock
    void eventuallyNot(Closure predicate) {
        try {
            new PollingConditions(timeout: timeout, delay: delay).eventually {
                predicate()
            }
            throw new PredicateEventuallyFulfilled()
        } catch (AssertionError _) {
            // Predicate not fulfilled and that's ok
        }
    }
}

class PredicateEventuallyFulfilled extends RuntimeException {
}
