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

package org.spockframework.runtime;

import org.spockframework.runtime.model.*;
import spock.config.RunnerConfiguration;

import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Adds the ability to run parameterized features.
 *
 * @author Peter Niederwieser
 */
public class PlatformParameterizedSpecRunner extends PlatformSpecRunner {
  public PlatformParameterizedSpecRunner(IRunSupervisor supervisor) {
    super(supervisor);
  }

  @Override
  void runParameterizedFeature(SpockExecutionContext context, ParameterizedFeatureChildExecutor childExecutor) throws InterruptedException {
    if (context.getErrorInfoCollector().hasErrors()) {
      return;
    }

    Object[] dataProviders = createDataProviders(context);
    int numIterations = estimateNumIterations(context, dataProviders);
    Iterator[] iterators = createIterators(context, dataProviders);
    runIterations(context, childExecutor, iterators, numIterations);
    try {
      childExecutor.awaitFinished();
    } finally {
      closeDataProviders(dataProviders);
    }
  }

  private Object[] createDataProviders(SpockExecutionContext context) {
    if (context.getErrorInfoCollector().hasErrors()) {
      return null;
    }

    List<DataProviderInfo> dataProviderInfos = context.getCurrentFeature().getDataProviders();
    if (dataProviderInfos.isEmpty()) {
      return new Object[0];
    }

    List<String> dataProviderVariables = dataProviderInfos
        .stream()
        .map(DataProviderInfo::getDataVariables)
        .flatMap(List::stream)
        .collect(toList());

    Object[] dataProviders = new Object[dataProviderInfos.size()];

    for (int i = 0, size = dataProviderInfos.size(); i < size; i++) {
      DataProviderInfo dataProviderInfo = dataProviderInfos.get(i);
      MethodInfo method = dataProviderInfo.getDataProviderMethod();

      Object provider = invokeRaw(
          context, context.getCurrentInstance(), method,
          getPreviousDataTableProviders(dataProviderVariables, dataProviders, dataProviderInfo));

      if (context.getErrorInfoCollector().hasErrors()) {
        if (provider != null) {
          dataProviders[i] = provider;
        }
        break;
      } else if (provider == null) {
        SpockExecutionException error = new SpockExecutionException("Data provider is null!");
        supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(method, error));
        break;
      }

      dataProviders[i] = provider;
    }

    return dataProviders;
  }

  private Object[] getPreviousDataTableProviders(List<String> dataProviderVariables, Object[] dataProviders,
                                                 DataProviderInfo dataProviderInfo) {
    List<Object> result = new ArrayList<>();
    previousDataTableVariablesLoop:
    for (String previousDataTableVariable : dataProviderInfo.getPreviousDataTableVariables()) {
      for (int i = 0, size = dataProviderVariables.size(); i < size; i++) {
        String dataProviderVariable = dataProviderVariables.get(i);
        if (previousDataTableVariable.equals(dataProviderVariable)) {
          result.add(dataProviders[i]);
          continue previousDataTableVariablesLoop;
        }
      }
      throw new IllegalStateException(String.format("Variable name not defined (%s not in %s)!",
          previousDataTableVariable, dataProviderVariables));
    }
    return result.toArray();
  }

  private Iterator[] createIterators(SpockExecutionContext context, Object[] dataProviders) {
    if (context.getErrorInfoCollector().hasErrors()) {
      return null;
    }

    Iterator[] iterators = new Iterator<?>[dataProviders.length];
    for (int i = 0; i < dataProviders.length; i++)
      try {
        Iterator<?> iter = GroovyRuntimeUtil.asIterator(dataProviders[i]);
        if (iter == null) {
          supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(context.getCurrentFeature().getDataProviders().get(i).getDataProviderMethod(),
              new SpockExecutionException("Data provider's iterator() method returned null")));
          return null;
        }
        iterators[i] = iter;
      } catch (Throwable t) {
        supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(context.getCurrentFeature().getDataProviders().get(i).getDataProviderMethod(), t));
        return null;
      }

    return iterators;
  }

  // -1 => unknown
  private int estimateNumIterations(SpockExecutionContext context, Object[] dataProviders) {
    if (context.getErrorInfoCollector().hasErrors()) {
      return -1;
    }
    if (dataProviders.length == 0) {
      return 1;
    }

    int result = Integer.MAX_VALUE;
    for (Object prov : dataProviders) {
      if (prov instanceof Iterator)
      // unbelievably, DGM provides a size() method for Iterators,
      // although it is of course destructive (i.e. it exhausts the Iterator)
      {
        continue;
      }

      Object rawSize = GroovyRuntimeUtil.invokeMethodQuietly(prov, "size");
      if (!(rawSize instanceof Number)) {
        continue;
      }

      int size = ((Number)rawSize).intValue();
      if (size < 0 || size >= result) {
        continue;
      }

      result = size;
    }

    return result == Integer.MAX_VALUE ? -1 : result;
  }

  private void runIterations(SpockExecutionContext context, ParameterizedFeatureChildExecutor childExecutor, Iterator[] iterators, int estimatedNumIterations) {
    if (context.getErrorInfoCollector().hasErrors()) {
      return;
    }

    int iterationIndex = 0;
    while (haveNext(context, iterators)) {
      IterationInfo iterationInfo = createIterationInfo(context, iterationIndex, nextArgs(context, iterators), estimatedNumIterations);
      IterationNode iterationNode = new IterationNode(
          context.getParentId().append("iteration", String.valueOf(iterationIndex++)),
          context.getRunContext().getConfiguration(RunnerConfiguration.class), iterationInfo);

      if (context.getErrorInfoCollector().hasErrors()) {
        return;
      }
      if (iterationInfo.getFeature().getIterationFilter().isAllowed(iterationInfo.getIterationIndex())) {
        childExecutor.execute(iterationNode);
      }

      // no iterators => no data providers => only derived parameterizations => limit to one iteration
      if (iterators.length == 0) {
        break;
      }
    }
  }

  private void closeDataProviders(Object[] dataProviders) {
    if (dataProviders == null) {
      return; // there was an error creating the providers
    }

    for (Object provider : dataProviders) {
      GroovyRuntimeUtil.invokeMethodQuietly(provider, "close");
    }
  }

  private boolean haveNext(SpockExecutionContext context, Iterator[] iterators) {
    if (context.getErrorInfoCollector().hasErrors()) {
      return false;
    }

    boolean haveNext = true;

    for (int i = 0; i < iterators.length; i++)
      try {
        boolean hasNext = iterators[i].hasNext();
        if (i == 0) {
          haveNext = hasNext;
        } else if (haveNext != hasNext) {
          DataProviderInfo provider = context.getCurrentFeature().getDataProviders().get(i);
          supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(provider.getDataProviderMethod(),
              createDifferentNumberOfDataValuesException(provider, hasNext)));
          return false;
        }

      } catch (Throwable t) {
        supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(context.getCurrentFeature().getDataProviders().get(i).getDataProviderMethod(), t));
        return false;
      }

    return haveNext;
  }

  private SpockExecutionException createDifferentNumberOfDataValuesException(DataProviderInfo provider,
                                                                             boolean hasNext) {
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

  // advances iterators and computes args
  private Object[] nextArgs(SpockExecutionContext context, Iterator[] iterators) {
    if (context.getErrorInfoCollector().hasErrors()) {
      return null;
    }

    Object[] next = new Object[iterators.length];
    for (int i = 0; i < iterators.length; i++)
      try {
        next[i] = iterators[i].next();
      } catch (Throwable t) {
        supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(context.getCurrentFeature().getDataProviders().get(i).getDataProviderMethod(), t));
        return null;
      }

    try {
      return (Object[])invokeRaw(context, context.getSharedInstance(), context.getCurrentFeature().getDataProcessorMethod(), next);
    } catch (Throwable t) {
      supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(context.getCurrentFeature().getDataProcessorMethod(), t));
      return null;
    }
  }
}
