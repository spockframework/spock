/*
 * Copyright 2012 the original author or authors.
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

package spock.util.concurrent;

import java.util.concurrent.TimeUnit;

import groovy.lang.Closure;

import org.spockframework.lang.ConditionBlock;
import org.spockframework.runtime.GroovyRuntimeUtil;
import org.spockframework.runtime.SpockTimeoutError;
import org.spockframework.util.Beta;

/**
 * Repeatedly evaluates one or more conditions until they are satisfied or a timeout has elapsed.
 * The timeout and delays between evaluation attempts are configurable.
 *
 * <p>Usage example:</p>
 *
 * <pre>
 * def conditions = new PollingConditions(timeout: 10, initialDelay: 1, factor: 1.25)
 * def machine = new Machine()
 *
 * when:
 * machine.start()
 *
 * then:
 * conditions.eventually {
 *   assert machine.temperature >= 100
 *   assert machine.efficiency >= 0.9
 * }
 * </pre>
 */
@Beta
public class PollingConditions {
  private long timeout = 5000;
  private long initialDelay = 0;
  private long delay = 500;
  private double factor = 1.0;

  /**
   * Same as {@code setTimeout(seconds, TimeUnit.SECONDS)}.
   */
  public void setTimeout(int seconds) {
    setTimeout(seconds, TimeUnit.SECONDS);
  }

  /**
   * Sets the timeout until which the conditions have to be satisfied.
   * Defaults to five seconds.
   *
   * @param value the timeout until which the conditions have to be satisfied
   * @param unit the value's time unit
   */
  public void setTimeout(long value, TimeUnit unit) {
    timeout = unit.toMillis(value);
  }

  /**
   * Same as {@code setInitialDelay(seconds, TimeUnit.SECONDS)}.
   */
  public void setInitialDelay(int seconds) {
    setInitialDelay(seconds, TimeUnit.SECONDS);
  }

  /**
   * Sets the initial delay before first evaluating the conditions.
   * Defaults to zero seconds.
   *
   * @param value the initial delay before first evaluating the conditions
   * @param unit the value's time unit
   */
  public void setInitialDelay(long value, TimeUnit unit) {
    initialDelay = unit.toMillis(value);
  }

  /**
   * Same as {@code setDelay(seconds, TimeUnit.SECONDS)}.
   */
  public void setDelay(int seconds) {
    setDelay(seconds, TimeUnit.SECONDS);
  }

  /**
   * Sets the delay between successive evaluations of the conditions.
   * Defaults to one second.
   *
   * @param value the delay between successive evaluations of the conditions.
   * @param unit the value's time unit
   */
  public void setDelay(long value, TimeUnit unit) {
    delay = unit.toMillis(value);
  }

  /**
   * Sets the factor by which the delay grows (or shrinks) after each evaluation of the conditions.
   * Defaults to 1.
   *
   * @param factor the factor by which the delay grows (or shrinks) after each evaluation of the conditions
   */
  public void setFactor(double factor) {
    this.factor = factor;
  }

  /**
   * Repeatedly evaluates the specified conditions until they are satisfied or the timeout has elapsed.
   *
   * @param conditions the conditions to evaluate
   *
   * @throws InterruptedException if evaluation is interrupted
   */
  @ConditionBlock
  public void eventually(Closure<?> conditions) throws InterruptedException {
    within(timeout, TimeUnit.MILLISECONDS, conditions);
  }

  /**
   * Same as {@code within(seconds, TimeUnit.SECONDS, conditions)}.
   */
  @ConditionBlock
  public void within(int seconds, Closure<?> conditions) throws InterruptedException {
    within(seconds, TimeUnit.SECONDS, conditions);
  }

  /**
   * Repeatedly evaluates the specified conditions until they are satisfied or the specified timeout has elapsed.
   *
   * @param conditions the conditions to evaluate
   *
   * @throws InterruptedException if evaluation is interrupted
   */
  @ConditionBlock
  public void within(long value, TimeUnit unit, Closure<?> conditions) throws InterruptedException  {
    long timeout = unit.toMillis(value);
    long start = System.currentTimeMillis();
    long lastAttempt = 0;
    Thread.sleep(initialDelay);

    long currDelay = delay;
    int attempts = 0;

    while(true) {
      try {
        attempts++;
        lastAttempt = System.currentTimeMillis();
        GroovyRuntimeUtil.invokeClosure(conditions);
        return;
      } catch (AssertionError e) {
        long elapsedTime = lastAttempt - start;
        if (elapsedTime >= timeout) {
          String msg = String.format("Condition not satisfied after %.2f seconds and %d attempts", elapsedTime / 1000d, attempts);
          throw new SpockTimeoutError(timeout, TimeUnit.MILLISECONDS, msg);
        }
        Thread.sleep(Math.min(currDelay, start + timeout - System.currentTimeMillis()));
        currDelay *= factor;
      }
    }
  }

  /**
   * Alias for {@link #eventually(groovy.lang.Closure)}.
   */
  @ConditionBlock
  public void call(Closure<?> conditions) throws InterruptedException {
    eventually(conditions);
  }

  /**
   * Alias for {@link #within(int, groovy.lang.Closure)}.
   */
  @ConditionBlock
  public void call(int seconds, Closure<?> conditions) throws InterruptedException {
    within(seconds, conditions);
  }

  /**
   * Alias for {@link #within(long, java.util.concurrent.TimeUnit, groovy.lang.Closure)}.
   */
  @ConditionBlock
  public void call(long value, TimeUnit unit, Closure<?> conditions) throws InterruptedException {
    within(value, unit, conditions);
  }
}
