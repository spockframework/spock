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

package spock.util.concurrent

import java.util.concurrent.TimeUnit

import org.spockframework.util.ThreadSafe
import org.spockframework.util.TimeUtil

/**
 * Provides an unlimited number of dynamic variables. Reading the value of a
 * variable will block until some other thread has set the value of the variable,
 * or a timeout expires. Useful for verifying state in an expect- or then-block
 * that has been captured in some other thread.
 *
 * <p>Example:
 *
 * <pre>
 * // create object under specification
 * def machine = new Machine()
 *
 * BlockingVariables vars = new BlockingVariables()
 *
 * // register async callback
 * machine.workDone << { result ->
 *  vars.result = result
 * }
 *
 * when:
 * machine.start()
 *
 * then:
 * // blocks until workDone callback has set result, or a timeout expires
 * vars.result == WorkResult.OK
 *
 * cleanup:
 * // shut down all threads
 * machine?.shutdown()
 * </pre>
 *
 * @author Peter Niederwieser
 */
@ThreadSafe
class BlockingVariables {
  private final BlockingVariablesImpl impl

  /**
   * Same as <tt>BlockingVariables(1, TimeUnit.SECONDS)</tt>.
   */
  BlockingVariables() {
    this(1)
  }

  /**
   * Instantiates a <tt>BlockingVariable</tt> with the specified timeout (in seconds).
   *
   * @param timeout the timeout (in seconds) for reading a variable's value.
   */
  BlockingVariables(double timeout) {
    impl = new BlockingVariablesImpl(timeout)
  }

  /**
   * Instantiates a <tt>BlockingVariables</tt> instance with the specified
   * timeout.
   *
   * @param timeout timeout for reading a variable's value
   * @param unit the timeout's time unit
   *
   * @deprecated use {@link #BlockingVariables(double)} instead
   */
  @Deprecated
  BlockingVariables(int timeout, TimeUnit unit) {
    this(TimeUtil.toSeconds(timeout, unit))
  }

  /**
   * Sets a variable's value. This method should not be called directly
   * but by setting a dynamic variable's value.
   *
   * @param name the variable's name
   * @param value the variable's value
   */
  void setProperty(String name, Object value) {
    impl.put(name, value)
  }

  /**
   * Gets a variable's value. Blocks until a value has been set for the variable
   * or a timeout expires. This method should not be called directly but by
   * getting a dynamic variable's value.
   *
   * @param name the variable's name
   *
   * @return the variable's value
   */
  Object getProperty(String name) {
    impl.get(name)
  }
}
