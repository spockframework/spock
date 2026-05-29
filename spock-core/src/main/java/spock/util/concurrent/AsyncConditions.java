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

import org.spockframework.lang.ConditionBlock;
import org.spockframework.runtime.SpockTimeoutError;
import org.spockframework.util.*;

import java.util.concurrent.*;

/**
 * Alternative to class <tt>BlockingVariable(s)</tt> that allows to evaluate conditions
 * in a thread other than the spec runner's thread(s). Rather than transferring
 * state to an expect- or then-block, it is verified right where it is captured.
 * On the upside, this can result in a more informative stack trace if the
 * evaluation of a condition fails. On the downside, the coordination between
 * threads is more explicit (number of <tt>evaluate</tt> blocks has to be specified if
 * greater than one), and the usual structure of a feature method cannot be
 * preserved (conditions are no longer located in expect-/then-block).
 *
 * <p>Example:
 * <pre>
 * // create object under specification
 * def machine = new Machine()
 *
 * def conds = new AsyncConditions()
 *
 * // register async callback
 * machine.workDone >> { result ->
 *   conds.evaluate {
 *     assert result == WorkResult.OK
 *     // could add more explicit conditions here
 *   }
 * }
 *
 * when:
 * machine.start()
 *
 * then:
 * // wait for the evaluation to complete
 * // any exception thrown in the evaluate block will be rethrown from this method
 * conds.await()
 *
 * cleanup:
 * // shut down all threads
 * machine?.shutdown()
 * <pre>
 *
 * @author Peter Niederwieser
 */
@ThreadSafe
public class AsyncConditions {
  private final int numEvalBlocks;
  private final CountDownLatch latch;
  private final ConcurrentLinkedQueue<Throwable> exceptions = new ConcurrentLinkedQueue<>();

  /**
   * Same as <tt>AsyncConditions(1)</tt>.
   */
  public AsyncConditions() {
    this(1);
  }

  /**
   * Instantiates an <tt>AsyncConditions</tt> instance with the specified number
   * of evaluate blocks. <tt>await()</tt> will block until all evaluate blocks
   * have completed or a timeout expires.
   *
   * <p>Note: One evaluate block may contain multiple conditions.
   *
   * @param numEvalBlocks the number of evaluate blocks that <tt>await()</tt>
   * should wait for
   */
  public AsyncConditions(int numEvalBlocks) {
    this.numEvalBlocks = numEvalBlocks;
    latch = new CountDownLatch(numEvalBlocks);
  }

  /**
   * Evaluates the specified block, which is expected to contain
   * one or more explicit conditions (i.e. assert statements).
   * Any caught exception will be rethrown from <tt>await()</tt>.
   *
   * @param block the code block to evaluate
   */
  @ConditionBlock
  public void evaluate(Runnable block) {
    try {
      block.run();
    } catch (Throwable t) {
      exceptions.add(t);
      wakeUp();
    }
    latch.countDown();
  }

  private void wakeUp() {
    // this will definitely wake us up; it doesn't matter that countDown()
    // might get called too often
    long pendingEvalBlocks = latch.getCount();
    for (int i = 0; i < pendingEvalBlocks; i++)
      latch.countDown();
  }

  /**
   * Same as {@code await(1)}.
   */
  public void await() throws Throwable {
    await(1);
  }

  /**
   * Waits until all evaluate blocks have completed or the specified timeout
   * (in seconds) expires. If one of the evaluate blocks throws an exception, it is rethrown
   * from this method.
   *
   * @param seconds the timeout (in seconds)
   * @throws InterruptedException if the calling thread is interrupted
   * @throws Throwable the first exception thrown by an evaluate block
   */
  public void await(double seconds) throws Throwable {
    boolean evalBlocksFinished = latch.await((long) (seconds * 1000), TimeUnit.MILLISECONDS);
    if (!exceptions.isEmpty())
      throw exceptions.poll();

    if (!evalBlocksFinished) {
      String msg = String.format("Async conditions timed out " +
          "after %1.2f seconds; %d out of %d evaluate blocks did not complete in time",
          seconds, latch.getCount(), numEvalBlocks);
      throw new SpockTimeoutError(seconds, msg);
    }
  }

  /**
   * Waits until all evaluate blocks have completed or the specified timeout
   * expires. If one of the evaluate blocks throws an exception, it is rethrown
   * from this method.
   *
   * @throws InterruptedException if the calling thread is interrupted
   *
   * @throws Throwable the first exception thrown by an evaluate block
   *
   * @deprecated use {@link #await(double)} instead
   */
  @Deprecated
  public void await(int value, TimeUnit unit) throws Throwable {
    await(TimeUtil.toSeconds(value, unit));
  }
}
