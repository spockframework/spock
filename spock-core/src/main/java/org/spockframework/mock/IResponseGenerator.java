/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.mock;

import org.spockframework.util.Beta;
import org.spockframework.util.Nullable;

import java.util.function.Supplier;

/**
 * Generates responses to mock invocations.
 */
@Beta
public interface IResponseGenerator {
  /**
   * Returns the response to be made for the given invocation. It is preferable to call
   * {@link #getResponseSupplier(IMockInvocation)} instead, as that method can separate immediate actions that might
   * need to be done under some lock from actions that can be done later in the returned supplier.
   *
   * <p>The default implementation of this method uses {@code getResponseSupplier}, the default implementation of which
   * uses this method. Implementations of this interface should override exactly one of these two methods. If they
   * override both, the result could be different depending on which method is called, if they override none,
   * any call will result in a {@code StackOverflowError}. Calls to the two methods must always behave consistent,
   * and the easiest way to achieve that is to override only one of them.
   *
   * <p>If you implement this interface and do not need to update shared state immediately like when implementing
   * {@link IChainableResponseGenerator}, you can just override this method. If you need the separation of immediate
   * actions done under some lock and actions that can be done delayed, better override {@code getResponseSupplier}.
   *
   * @param invocation The invocation to generate a response for
   * @return The generated response
   */
  @Nullable
  default Object respond(IMockInvocation invocation) {
    return getResponseSupplier(invocation).get();
  }

  /**
   * Returns a supplier with the response to be made for the given invocation.
   *
   * <p>The default implementation of this method uses {@link #respond(IMockInvocation)}, the default implementation
   * of which uses this method. Implementations of this interface should override exactly one of these two methods.
   * If they override both, the result could be different depending on which method is called, if they override none,
   * any call will result in a {@code StackOverflowError}. Calls to the two methods must always behave consistent,
   * and the easiest way to achieve that is to override only one of them.
   *
   * <p>If you implement this interface and do need to update shared state immediately like when implementing
   * {@link IChainableResponseGenerator}, you should prefer overriding this method. If not, then you might consider
   * just overriding {@code respond} instead.
   *
   * @param invocation The invocation to generate a response for
   * @return A supplier that provides the generated response
   */
  default Supplier<Object> getResponseSupplier(IMockInvocation invocation) {
    return () -> respond(invocation);
  }
}
