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

package org.spockframework.mock;

/**
 * Generates return values for invocations on mock objects.
 *
 * @author Peter Niederwieser
 */
public interface IChainableResponseGenerator extends IResponseGenerator {
  /**
   * Whether this chainable response generator is at the end of its cycle. If this method returns {@code true},
   * the next response generator in the chain is used if one is available. If this method returns {@code false} or
   * there is no next response generator in the chain, the {@link #getResponseSupplier(IMockInvocation)} method will
   * be called further on for all matched invocations.
   *
   * <p>The {@code getResponseSupplier} method and this method will be called under a common lock to make sure code
   * in {@code getResponseSupplier} can update state like setting a boolean or advancing an iterator, that is then
   * checked in the implementation of this method. Such state updates should therefore be done within
   * {@code getResponseSupplier} directly and not within the returned {@code Supplier} or in
   * {@link #respond(IMockInvocation)}, otherwise the iteration matching algorithm will break. All other logic
   * should preferably be done within the {@code Supplier}, especially if the code could deadlock like closures
   * waiting for a common condition.
   *
   * @return Whether this chainable response generator is at the end of its cycle
   */
  boolean isAtEndOfCycle();
}
