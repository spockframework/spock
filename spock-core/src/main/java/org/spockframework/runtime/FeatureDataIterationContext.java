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

import org.spockframework.runtime.model.*;
import org.spockframework.util.Nullable;
import spock.config.RunnerConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The {@link IDataIterationContext} for a feature's {@code where:} block, backed by the
 * Spock execution context; errors are reported to the run supervisor.
 *
 * @since 2.5
 */
class FeatureDataIterationContext implements IDataIterationContext {
  private final IRunSupervisor supervisor;
  private final SpockExecutionContext context;
  private final boolean logFilteredIterations;
  private IStackTraceFilter stackTraceFilter;

  FeatureDataIterationContext(IRunSupervisor supervisor, SpockExecutionContext context) {
    this.supervisor = supervisor;
    this.context = context;

    RunnerConfiguration runnerConfiguration = context
      .getRunContext()
      .getConfiguration(RunnerConfiguration.class);
    logFilteredIterations = runnerConfiguration.logFilteredIterations;
    if (logFilteredIterations) {
      stackTraceFilter = runnerConfiguration.filterStackTrace ? new StackTraceFilter(context.getSpec()) : new DummyStackTraceFilter();
    }
  }

  @Override
  public boolean hasErrors() {
    return context.getErrorInfoCollector().hasErrors();
  }

  @Override
  public void error(@Nullable MethodInfo method, Throwable error) {
    supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(method, error, getErrorContext()));
  }

  private IErrorContext getErrorContext() {
    return ErrorContext.from((SpecificationContext) context.getCurrentInstance().getSpecificationContext());
  }

  @Override
  @Nullable
  public MethodInfo getWhereVariablesMethod() {
    return context.getCurrentFeature().getWhereVariablesMethod();
  }

  @Override
  public MethodInfo getDataProcessorMethod() {
    return context.getCurrentFeature().getDataProcessorMethod();
  }

  @Override
  public List<String> getProcessedDataVariableNames() {
    return Collections.unmodifiableList(Arrays.asList(
      context.getCurrentFeature().getDataProcessorMethod().getAnnotation(DataProcessorMetadata.class)
        .dataVariables()));
  }

  @Override
  @Nullable
  public MethodInfo getFilterMethod() {
    return context.getCurrentFeature().getFilterMethod();
  }

  @Override
  @Nullable
  public MethodInfo getDataVariableMultiplicationsMethod() {
    return context.getCurrentFeature().getDataVariableMultiplicationsMethod();
  }

  @Override
  public List<DataProviderInfo> getDataProviders() {
    return context.getCurrentFeature().getDataProviders();
  }

  @Override
  public Object getDataProviderTarget() {
    return context.getCurrentInstance();
  }

  @Override
  public Object getDataProcessorTarget() {
    return context.getSharedInstance();
  }

  @Override
  public boolean isLogFilteredIterations() {
    return logFilteredIterations;
  }

  @Override
  public IStackTraceFilter getStackTraceFilter() {
    return stackTraceFilter;
  }

  @Override
  public SpockExecutionException createDifferentNumberOfDataValuesException(DataProviderInfo provider, boolean hasNext) {
    String msg = String.format("Data provider for variable '%s' has %s values than previous data provider(s)",
      provider.getDataVariables().get(0), hasNext ? "more" : "fewer");
    SpockExecutionException exception = new SpockExecutionException(msg);
    FeatureInfo feature = provider.getParent();
    SpecInfo spec = feature.getParent();
    StackTraceElement elem = new StackTraceElement(spec.getReflection().getName(),
      feature.getName(), spec.getFilename(), provider.getLine());
    exception.setStackTrace(new StackTraceElement[]{elem});
    return exception;
  }
}
