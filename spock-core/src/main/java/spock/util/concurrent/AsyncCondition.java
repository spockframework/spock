/*
 * Copyright 2010 the original author or authors.
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

import java.util.concurrent.*;

import org.spockframework.runtime.SpockAssertionError;
import org.spockframework.runtime.SpockTimeoutError;

public class AsyncCondition {
  private final int numChecks;
  private final CountDownLatch latch;
  private final ConcurrentLinkedQueue<Throwable> exceptions = new ConcurrentLinkedQueue<Throwable>();

  public AsyncCondition() {
    this(1);
  }

  public AsyncCondition(int numChecks) {
    this.numChecks = numChecks;
    latch = new CountDownLatch(numChecks);
  }

  public void check(Runnable block) throws Throwable {
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
    long pendingChecks = latch.getCount();
    for (int i = 0; i < pendingChecks; i++)
      latch.countDown();
  }

  public void await() throws InterruptedException, Throwable {
    await(1, TimeUnit.SECONDS);
  }

  public void await(int timeout, TimeUnit unit) throws InterruptedException, Throwable {
    latch.await(timeout, unit);
    if (!exceptions.isEmpty())
      throw new SpockAssertionError("Async condition failed", exceptions.poll());
    
    long pendingChecks = latch.getCount();
    if (pendingChecks > 0)
      throw new SpockTimeoutError("Async conditions timed out; #d out of #d checks were executed in time")
        .withArgs(numChecks - pendingChecks, numChecks);
  }
}
