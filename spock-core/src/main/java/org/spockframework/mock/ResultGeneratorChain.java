/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.mock;

import org.spockframework.util.InternalSpockError;

import java.util.LinkedList;

public class ResultGeneratorChain implements IResultGenerator {
  private IResultGenerator current;
  private final LinkedList<IResultGenerator> remaining = new LinkedList<IResultGenerator>();
  
  public void addFirst(IResultGenerator generator) {
    if (current != null) remaining.addFirst(current);
    current = generator;
  }

  public boolean isEmpty() {
    return current == null;
  }

  public boolean isExhausted() {
    throw new InternalSpockError("isExhausted() isn't implemented for ResultGeneratorChain");
  }

  public Object generate(IMockInvocation invocation) {
    if (isEmpty()) throw new InternalSpockError("ResultGeneratorChain should never be empty");
    
    while (current.isExhausted() && !remaining.isEmpty()) {
      current = remaining.removeFirst();
    }
    return current.generate(invocation);
  }
}
