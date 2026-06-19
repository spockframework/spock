/*
 * Copyright 2026 the original author or authors.
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

package org.spockframework.runtime;

import org.spockframework.runtime.model.DataProviderInfo;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.util.Nullable;

import java.util.List;

/**
 * Abstraction over the environment a data-iterator chain runs in.
 * <p>
 * The feature path is backed by the Spock execution context and run supervisor
 * ({@code FeatureDataIterationContext}); errors are reported to the supervisor and
 * {@link #hasErrors()} subsequently turns {@code true}, so the chain short-circuits.
 * Standalone implementations have no supervisor: {@link #error(MethodInfo, Throwable)}
 * throws instead and {@link #hasErrors()} stays {@code false}.
 *
 * @since 2.5
 */
public interface IDataIterationContext {
  /**
   * @return whether an error has been reported; the iterator chain stops producing values once this is {@code true}
   */
  boolean hasErrors();

  /**
   * Reports an error that occurred while computing data values.
   * <p>
   * Feature-path implementations record the error and return normally (callers then return {@code null});
   * standalone implementations rethrow.
   *
   * @param method the generated method whose invocation failed, may be {@code null}
   * @param error the error that occurred
   */
  void error(@Nullable MethodInfo method, Throwable error);

  /**
   * @return the method computing the where-block variable values, or {@code null} if there are none
   */
  @Nullable
  MethodInfo getWhereVariablesMethod();

  /**
   * @return the data processor method turning raw provider values into data variable values
   */
  MethodInfo getDataProcessorMethod();

  /**
   * @return the names of the data variables produced by the data processor, in row order
   */
  List<String> getProcessedDataVariableNames();

  /**
   * @return the filter method dropping iterations, or {@code null} if there is none
   */
  @Nullable
  MethodInfo getFilterMethod();

  /**
   * @return the method providing the data variable multiplications, or {@code null} if there are none
   */
  @Nullable
  MethodInfo getDataVariableMultiplicationsMethod();

  /**
   * @return the data providers, in declaration order
   */
  List<DataProviderInfo> getDataProviders();

  /**
   * @return the invocation target for data provider and where-variables methods
   */
  @Nullable
  Object getDataProviderTarget();

  /**
   * @return the invocation target for data processor and filter methods
   */
  @Nullable
  Object getDataProcessorTarget();

  /**
   * @return whether iterations dropped by the filter method should be logged
   */
  boolean isLogFilteredIterations();

  /**
   * @return the stack trace filter used when logging filtered iterations; only consulted when
   * {@link #isLogFilteredIterations()} is {@code true}
   */
  IStackTraceFilter getStackTraceFilter();

  /**
   * Creates the exception reported when one data provider runs out of values before the others.
   *
   * @param provider the data provider that differs from its predecessors
   * @param hasNext whether the differing provider still has values
   * @return the exception to report
   */
  SpockExecutionException createDifferentNumberOfDataValuesException(DataProviderInfo provider, boolean hasNext);
}
