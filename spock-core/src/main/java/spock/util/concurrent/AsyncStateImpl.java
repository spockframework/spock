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

import org.spockframework.runtime.SpockTimeoutError;

class AsyncStateImpl {
  private final int timeout;
  private final TimeUnit unit;
  private final ConcurrentHashMap<String, AsyncVariable> map = new ConcurrentHashMap<String, AsyncVariable>();

  public AsyncStateImpl() {
    this(1, TimeUnit.SECONDS);
  }

  public AsyncStateImpl(int timeout, TimeUnit unit) {
    this.timeout = timeout;
    this.unit = unit;
  }
  
  public Object get(String name) throws InterruptedException {
    AsyncVariable entry = new AsyncVariable(name);
    AsyncVariable oldEntry = map.putIfAbsent(name, entry);
    if (oldEntry == null)
      return entry.getValue();
    else
      return oldEntry.getValue();
  }

  public void put(String name, Object value) {
    AsyncVariable entry = new AsyncVariable(name);
    AsyncVariable oldEntry = map.putIfAbsent(name, entry);
    if (oldEntry == null)
      entry.setValue(value);
    else
      oldEntry.setValue(value);
  }

  class AsyncVariable {
    private final String name;
    private Object value; // access guarded by valueReady
    private final CountDownLatch valueReady = new CountDownLatch(1);

    AsyncVariable(String name) {
      this.name = name;
    }

    Object getValue() throws InterruptedException {
      if (!valueReady.await(timeout, unit))
        throw new SpockTimeoutError("Timed out waiting for variable '%s'").withArgs(name);
      return value;
    }

    void setValue(Object value) {
      this.value = value;
      valueReady.countDown();
    }
  }
}
