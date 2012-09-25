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

package org.spockframework.mock.response;

import java.util.LinkedList;

import org.spockframework.mock.IChainableResponseGenerator;
import org.spockframework.mock.IMockInvocation;
import org.spockframework.mock.IResponseGenerator;
import org.spockframework.util.InternalSpockError;

public class ResponseGeneratorChain implements IResponseGenerator {
  private IChainableResponseGenerator current;
  private final LinkedList<IChainableResponseGenerator> remaining = new LinkedList<IChainableResponseGenerator>();
  
  public void addFirst(IChainableResponseGenerator generator) {
    if (current != null) remaining.addFirst(current);
    current = generator;
  }

  public boolean isEmpty() {
    return current == null;
  }

  public Object respond(IMockInvocation invocation) {
    if (isEmpty()) throw new InternalSpockError("ResultGeneratorChain should never be empty");
    
    while (current.isAtEndOfCycle() && !remaining.isEmpty()) {
      current = remaining.removeFirst();
    }
    return current.respond(invocation);
  }
}
