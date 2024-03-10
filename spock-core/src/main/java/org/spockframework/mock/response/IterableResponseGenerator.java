/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.mock.response;

import org.spockframework.mock.*;
import org.spockframework.runtime.GroovyRuntimeUtil;
import org.spockframework.util.ThreadSafe;

import java.util.Iterator;
import java.util.function.Supplier;

/**
 * Generates result values from an iterable object. If the iterator has no more
 * values left, the previous value is returned.
 *
 * @author Peter Niederwieser
 */
@ThreadSafe
public class IterableResponseGenerator implements IChainableResponseGenerator {
  private final Object LOCK = new Object();
  private final Iterator<?> iterator;
  private Object nextValue;

  public IterableResponseGenerator(Object iterable) {
    iterator = GroovyRuntimeUtil.asIterator(iterable);
  }

  @Override
  public boolean isAtEndOfCycle() {
    synchronized (LOCK) {
      return !iterator.hasNext();
    }
  }

  @Override
  public Supplier<Object> getResponseSupplier(IMockInvocation invocation) {
    synchronized (LOCK) {
      if (iterator.hasNext()) nextValue = iterator.next();
      Object response = GroovyRuntimeUtil.coerce(nextValue, invocation.getMethod().getReturnType());
      return () -> response;
    }
  }
}
