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

package org.spockframework.runtime.extension;

import org.spockframework.runtime.model.*;
import org.spockframework.util.Beta;
import org.spockframework.util.Nullable;

/**
 * @author Peter Niederwieser
 */
public interface IMethodInvocation {
  /**
   * Returns the specification which this method invocation belongs to.
   *
   * @return the specification which this method invocation belongs to
   */
  SpecInfo getSpec();

  /**
   * Returns the feature which this method invocation belongs to (if any).
   * Differs from {@code MethodInfo.getFeature()} in that it reflects the dynamic
   * picture. For example, when a setup method is invoked, this method
   * will return the corresponding feature, whereas {@code MethodInfo.getFeature()}
   * will return {@code null}.
   *
   * @return the feature which this method invocation belongs to
   */
  @Nullable
  FeatureInfo getFeature();

  /**
   * Return the iteration which this method invocation belongs to (if any).
   * Executing a feature results in at least one but possibly more iterations
   * (e.g. for a data-driven feature).
   *
   * @return the iteration which this method invocation belongs to
   */
  @Nullable
  IterationInfo getIteration();

  /**
   * Returns the {@code Specification} instance for {@code @Shared} fields.
   *
   * <p>Note that in most cases, it's more appropriate to use the context-aware
   * {@link #getInstance}.
   *
   * @return the {@code Specification} instance for {@code @Shared}fields
   */
  Object getSharedInstance();

  /**
   * Returns the {@code Specification} instance for the current iteration.
   * For methods that operate in a {@code @Shared} context (e.g. {@code setupSpec}),
   * this method returns the same value as {@link #getSharedInstance}.
   *
   * @return the {@code Specification} instance for the current iteration
   */
  Object getInstance();

  /**
   * Returns the target (receiver) of this method invocation.
   * In case of a static method call, a <tt>Class<tt> instance
   * is returned.
   *
   * <p>Note that the target of the method invocation may not
   * necessarily be the {@code Specification} instance. That's why in
   * most cases, it's more appropriate to use {@code #getInstance}.
   *
   * @return the target (receiver) of this method invocation
   */
  Object getTarget();

  /**
   * Returns the method invoked by this method invocation.
   *
   * @return the method invoked by this method invocation
   */
  MethodInfo getMethod();

  /**
   * Returns the arguments for this method invocation.
   *
   * @return the arguments for this method invocation
   */
  Object[] getArguments();

  /**
   * Sets the arguments for this method invocation.
   *
   * @deprecated set the fields in the return value of {@link #getArguments()} instead,
   * or use {@link #resolveArgument(int, Object)}.
   */
  @Deprecated
  void setArguments(Object[] arguments);

  /**
   * Sets a missing argument value.
   * <p>
   * It will throw an exception if the argument is already set.
   * @param index the index of the argument
   * @param value the value of the argument
   * @since 2.4
   */
  @Beta
  void resolveArgument(int index, Object value);

  /**
   * Proceeds with the method call. Always call this method
   * unless you want to suppress the method call.
   *
   * @throws Throwable any exception thrown by the method call
   */
  void proceed() throws Throwable;
}
