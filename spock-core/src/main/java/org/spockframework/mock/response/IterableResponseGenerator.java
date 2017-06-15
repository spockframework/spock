/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import java.util.Iterator;

/**
 * Generates result values from an iterable object. If the iterator has no more
 * values left, the previous value is returned.
 *
 * @author Peter Niederwieser
 */
public class IterableResponseGenerator implements IChainableResponseGenerator {
  private final Iterator<?> iterator;
  private Object nextValue;

  public IterableResponseGenerator(Object iterable) {
    iterator = GroovyRuntimeUtil.asIterator(iterable);
  }

  @Override
  public boolean isAtEndOfCycle() {
    return !iterator.hasNext();
  }

  @Override
  public Object respond(IMockInvocation invocation) {
    if (iterator.hasNext()) nextValue = iterator.next();
    return GroovyRuntimeUtil.coerce(nextValue, invocation.getMethod().getReturnType());
  }
}
