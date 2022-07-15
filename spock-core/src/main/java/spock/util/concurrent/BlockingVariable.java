/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spock.util.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.spockframework.runtime.SpockTimeoutError;
import org.spockframework.util.ThreadSafe;
import org.spockframework.util.TimeUtil;

/**
 * A statically typed variable whose get() method will block until some other
 * thread has set a value with the set() method, or a timeout expires. Useful
 * for verifying state in an expect- or then-block that has been captured in
 * some other thread.
 *
 * <p>Example:
 * <pre>
 * // create object under specification
 * def machine = new Machine()
 *
 * def result = new BlockingVariable&lt;WorkResult&gt;
 *
 * // register async callback
 * machine.workDone >> { r ->
 *  result.set(r)
 * }
 *
 * when:
 * machine.start()
 *
 * then:
 * // blocks until workDone callback has set result, or a timeout expires
 * result.get() == WorkResult.OK
 *
 * cleanup:
 * // shut down all threads
 * machine?.shutdown()
 * </pre>
 *
 * @param <T> the variable's type
 *
 * @author Peter Niederwieser
 */
@ThreadSafe
public class BlockingVariable<T> {
  private final double timeout;

  private T value; // access guarded by valueReady
  private final CountDownLatch valueReady = new CountDownLatch(1);

  /**
   * Same as <tt>BlockingVariable(1)</tt>.
   */
  public BlockingVariable() {
    this(1);
  }

  /**
   * Instantiates a <tt>BlockingVariable</tt> with the specified timeout in seconds.
   *
   * @param timeout the timeout (in seconds) for calls to <tt>get()</tt>.
   */
  public BlockingVariable(double timeout) {
    this.timeout = timeout;
  }

  /**
   * Instantiates a <tt>BlockingVariable</tt> with the specified timeout.
   *
   * @param timeout the timeout for calls to <tt>get()</tt>.
   * @param unit the time unit
   *
   * @deprecated use {@link #BlockingVariable(double)} instead
   */
  @Deprecated
  public BlockingVariable(int timeout, TimeUnit unit) {
    this(TimeUtil.toSeconds(timeout, unit));
  }

  /**
   * Returns the timeout (in seconds).
   *
   * @return the timeout (in seconds)
   */
  public double getTimeout() {
    return timeout;
  }

  /**
   * Blocks until a value has been set for this variable, or a timeout expires.
   *
   * @return the variable's value
   *
   * @throws InterruptedException if the calling thread is interrupted
   */
  public T get() throws InterruptedException {
    if (!valueReady.await((long) (timeout * 1000), TimeUnit.MILLISECONDS)) {
      String msg = String.format("BlockingVariable.get() timed out after %1.2f seconds", timeout);
      throw new SpockTimeoutError(timeout, msg);
    }
    return value;
  }

  /**
   * Sets a value for this variable. Wakes up all threads blocked in <tt>get()</tt>.
   *
   * @param value the value to be set for this variable
   */
  public void set(T value) {
    this.value = value;
    valueReady.countDown();
  }
}
