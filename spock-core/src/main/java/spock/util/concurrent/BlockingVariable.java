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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.spockframework.runtime.SpockTimeoutError;

public class BlockingVariable<T> {
  private final int timeout;
  private final TimeUnit unit;

  private T value; // access guarded by valueReady
  private final CountDownLatch valueReady = new CountDownLatch(1);

  public BlockingVariable() {
    this(1, TimeUnit.SECONDS);
  }

  public BlockingVariable(int timeout, TimeUnit unit) {
    this.timeout = timeout;
    this.unit = unit;
  }

  public T get() throws InterruptedException {
    if (!valueReady.await(timeout, unit))
      throw new SpockTimeoutError("BlockingVariable.getValue() timed out after %d %s")
          .withArgs(timeout, unit.toString().toLowerCase());
    return value;
  }

  public void set(T value) {
    this.value = value;
    valueReady.countDown();
  }
}