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

import groovy.lang.Closure;
import org.spockframework.runtime.model.*;
import org.spockframework.util.ExceptionUtil;
import org.spockframework.util.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The {@link IDataIterationContext} for a standalone {@code @DataProvider} method, fed with the
 * closures the transform emitted inline into the method body. There is no supervisor: errors are
 * rethrown immediately and {@link #hasErrors()} stays {@code false}.
 *
 * @since 2.5
 */
class StandaloneDataIterationContext implements IDataIterationContext {
  private final List<String> dataVariableNames;
  @Nullable
  private final MethodInfo whereVariablesMethod;
  private final List<DataProviderInfo> dataProviders;
  private final MethodInfo dataProcessorMethod;
  @Nullable
  private final MethodInfo filterMethod;
  @Nullable
  private final MethodInfo dataVariableMultiplicationsMethod;

  StandaloneDataIterationContext(String[] dataVariableNames, @Nullable Closure<?> whereVariables,
                                 StandaloneDataProviderDescriptor[] dataProviders, Closure<?> dataProcessor,
                                 @Nullable Closure<?> filter, @Nullable Closure<?> dataVariableMultiplications) {
    this.dataVariableNames = Collections.unmodifiableList(Arrays.asList(dataVariableNames));
    whereVariablesMethod = createMethodInfo(whereVariables, MethodKind.WHERE_VARIABLES, "where variables");
    this.dataProviders = createDataProviderInfos(dataProviders);
    dataProcessorMethod = createMethodInfo(dataProcessor, MethodKind.DATA_PROCESSOR, "data processor");
    filterMethod = createMethodInfo(filter, MethodKind.FILTER, "filter");
    dataVariableMultiplicationsMethod =
      createMethodInfo(dataVariableMultiplications, MethodKind.DATA_VARIABLE_MULTIPLICATIONS, "data variable multiplications");
  }

  private static List<DataProviderInfo> createDataProviderInfos(StandaloneDataProviderDescriptor[] descriptors) {
    List<DataProviderInfo> result = new ArrayList<>(descriptors.length);
    for (StandaloneDataProviderDescriptor descriptor : descriptors) {
      DataProviderInfo providerInfo = new DataProviderInfo();
      providerInfo.setDataVariables(descriptor.getDataVariables());
      providerInfo.setPreviousDataTableVariables(descriptor.getPreviousDataTableVariables());
      providerInfo.setLine(descriptor.getLine());
      providerInfo.setDataProviderMethod(createMethodInfo(descriptor.getCode(), MethodKind.DATA_PROVIDER,
        "data provider for " + descriptor.getDataVariables()));
      result.add(providerInfo);
    }
    return Collections.unmodifiableList(result);
  }

  @Nullable
  private static MethodInfo createMethodInfo(@Nullable Closure<?> code, MethodKind kind, String name) {
    if (code == null) {
      return null;
    }
    MethodInfo methodInfo = new MethodInfo((Object target, Object... arguments) -> code.call(arguments));
    methodInfo.setKind(kind);
    methodInfo.setName(name);
    return methodInfo;
  }

  @Override
  public boolean hasErrors() {
    return false;
  }

  @Override
  public void error(@Nullable MethodInfo method, Throwable error) {
    ExceptionUtil.sneakyThrow(error);
  }

  @Override
  @Nullable
  public MethodInfo getWhereVariablesMethod() {
    return whereVariablesMethod;
  }

  @Override
  public MethodInfo getDataProcessorMethod() {
    return dataProcessorMethod;
  }

  @Override
  public List<String> getProcessedDataVariableNames() {
    return dataVariableNames;
  }

  @Override
  @Nullable
  public MethodInfo getFilterMethod() {
    return filterMethod;
  }

  @Override
  @Nullable
  public MethodInfo getDataVariableMultiplicationsMethod() {
    return dataVariableMultiplicationsMethod;
  }

  @Override
  public List<DataProviderInfo> getDataProviders() {
    return dataProviders;
  }

  @Override
  @Nullable
  public Object getDataProviderTarget() {
    return null;
  }

  @Override
  @Nullable
  public Object getDataProcessorTarget() {
    return null;
  }

  @Override
  public boolean isLogFilteredIterations() {
    return false;
  }

  @Override
  public IStackTraceFilter getStackTraceFilter() {
    throw new UnsupportedOperationException("getStackTraceFilter");
  }

  @Override
  public SpockExecutionException createDifferentNumberOfDataValuesException(DataProviderInfo provider, boolean hasNext) {
    return new SpockExecutionException(String.format("Data provider for variable '%s' has %s values than previous data provider(s)",
      provider.getDataVariables().get(0), hasNext ? "more" : "fewer"));
  }
}
