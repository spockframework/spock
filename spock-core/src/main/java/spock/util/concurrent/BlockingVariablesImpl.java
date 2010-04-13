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

class BlockingVariablesImpl {
  private final int timeout;
  private final TimeUnit unit;
  private final ConcurrentHashMap<String, BlockingVariable<Object>> map =
      new ConcurrentHashMap<String, BlockingVariable<Object>>();

  public BlockingVariablesImpl() {
    this(1, TimeUnit.SECONDS);
  }

  public BlockingVariablesImpl(int timeout, TimeUnit unit) {
    this.timeout = timeout;
    this.unit = unit;
  }
  
  public Object get(String name) throws InterruptedException {
    BlockingVariable<Object> entry = new BlockingVariable<Object>(timeout, unit);
    BlockingVariable<Object> oldEntry = map.putIfAbsent(name, entry);
    if (oldEntry == null)
      return entry.get();
    else
      return oldEntry.get();
  }

  public void put(String name, Object value) {
    BlockingVariable<Object> entry = new BlockingVariable<Object>(timeout, unit);
    BlockingVariable<Object> oldEntry = map.putIfAbsent(name, entry);
    if (oldEntry == null)
      entry.set(value);
    else
      oldEntry.set(value);
  }
}
