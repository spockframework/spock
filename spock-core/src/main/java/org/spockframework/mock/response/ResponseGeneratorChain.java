/*
 * Copyright 2012 the original author or authors.
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
import org.spockframework.util.InternalSpockError;
import org.spockframework.util.ThreadSafe;

import java.util.LinkedList;
import java.util.function.Supplier;

@ThreadSafe
public class ResponseGeneratorChain implements IResponseGenerator {
  private final Object LOCK = new Object();
  private IChainableResponseGenerator current;
  private final LinkedList<IChainableResponseGenerator> remaining = new LinkedList<>();

  public void addFirst(IChainableResponseGenerator generator) {
    synchronized (LOCK) {
      if (current != null) remaining.addFirst(current);
      current = generator;
    }
  }

  public boolean isEmpty() {
    synchronized (LOCK) {
      return current == null;
    }
  }

  @Override
  public Supplier<Object> respond(IMockInvocation invocation) {
    synchronized (LOCK) {
      if (isEmpty()) throw new InternalSpockError("ResultGeneratorChain should never be empty");

      while (current.isAtEndOfCycle() && !remaining.isEmpty()) {
        current = remaining.removeFirst();
      }
      return current.respond(invocation);
    }
  }
}
