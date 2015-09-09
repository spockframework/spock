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

import java.util.*;

import static org.spockframework.runtime.RunStatus.*;
import org.spockframework.runtime.model.*;
import org.spockframework.runtime.GroovyRuntimeUtil;

/**
 * Adds the ability to run parameterized features.
 *
 * @author Peter Niederwieser
 */
public class ParameterizedSpecRunner extends BaseSpecRunner {
  public ParameterizedSpecRunner(SpecInfo spec, IRunSupervisor supervisor) {
    super(spec, supervisor);
  }

  @Override
  protected void runParameterizedFeature() {
    if (runStatus != OK) return;

    List<Object> dataProviders = createDataProviders();
    int numIterations = estimateNumIterations(dataProviders);
    Iterator[] iterators = createIterators(dataProviders);
    runIterations(iterators, numIterations);
    closeDataProviders(dataProviders);
  }

  private List<Object> createDataProviders() {
    if (runStatus != OK) return Collections.emptyList();

    List<Object> dataProviders = new ArrayList<Object>();

    for (DataProviderInfo dataProviderInfo : currentFeature.getDataProviders()) {
      MethodInfo method = dataProviderInfo.getDataProviderMethod();
      Object provider = invokeRaw(sharedInstance, method, EMPTY_ARGS);
      if (runStatus != OK)
        return Collections.emptyList();
      else if (provider == null) {
        runStatus = supervisor.error(new ErrorInfo(method, new SpockExecutionException("Data provider is null")));
        return Collections.emptyList();
      }
      dataProviders.add(provider);
    }

    return dataProviders;
  }

  private Iterator[] createIterators(List<Object> dataProviders) {
    if (runStatus != OK) return null;

    Iterator[] iterators = new Iterator<?>[dataProviders.size()];
    for (int i = 0; i < dataProviders.size(); i++)
      try {
        Iterator<?> iter = GroovyRuntimeUtil.asIterator(dataProviders.get(i));
        if (iter == null) {
          runStatus = supervisor.error(
              new ErrorInfo(currentFeature.getDataProviders().get(i).getDataProviderMethod(),
              new SpockExecutionException("Data provider's iterator() method returned null")));
          return null;
        }
        iterators[i] = iter;
      } catch (Throwable t) {
        runStatus = supervisor.error(
            new ErrorInfo(currentFeature.getDataProviders().get(i).getDataProviderMethod(), t));
        return null;
      }

    return iterators;
  }

  // -1 => unknown
  private int estimateNumIterations(List<Object> dataProviders) {
    if (runStatus != OK) return -1;
    else if (dataProviders.isEmpty()) return 1;

    int result = Integer.MAX_VALUE;
    for (Object prov : dataProviders) {
      if (prov instanceof Iterator)
        // unbelievably, DGM provides a size() method for Iterators,
        // although it is of course destructive (i.e. it exhausts the Iterator)
        continue;

      Object rawSize = GroovyRuntimeUtil.invokeMethodQuietly(prov, "size");
      if (!(rawSize instanceof Number)) continue;

      int size = ((Number) rawSize).intValue();
      if (size < 0 || size >= result) continue;

      result = size;
    }

    return result == Integer.MAX_VALUE ? -1 : result;
  }

  private void runIterations(Iterator[] iterators, int estimatedNumIterations) {
    if (runStatus != OK) return;

    while (haveNext(iterators)) {
      initializeAndRunIteration(nextArgs(iterators).toArray(), estimatedNumIterations);

      if (resetStatus(ITERATION) != OK) break;
      // no iterators => no data providers => only derived parameterizations => limit to one iteration
      if(iterators.length == 0) break;
    }
  }

  private void closeDataProviders(List<Object> dataProviders) {
    if (action(runStatus) == ABORT) return;
    if (dataProviders == null) return; // there was an error creating the providers

    for (Object provider : dataProviders) {
      GroovyRuntimeUtil.invokeMethodQuietly(provider, "close");
    }
  }

  private boolean haveNext(Iterator[] iterators) {
    if (runStatus != OK) return false;

    boolean haveNext = true;

    for (int i = 0; i < iterators.length; i++)
      try {
        boolean hasNext = iterators[i].hasNext();
        if (i == 0) haveNext = hasNext;
        else if (haveNext != hasNext) {
          DataProviderInfo provider = currentFeature.getDataProviders().get(i);
          runStatus = supervisor.error(new ErrorInfo(provider.getDataProviderMethod(),
              createDifferentNumberOfDataValuesException(provider, hasNext)));
          return false;
        }

      } catch (Throwable t) {
        runStatus = supervisor.error(
            new ErrorInfo(currentFeature.getDataProviders().get(i).getDataProviderMethod(), t));
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
    exception.setStackTrace(new StackTraceElement[] { elem });
    return exception;
  }

  // advances iterators and computes args
  private List<Object> nextArgs(Iterator[] iterators) {
    if (runStatus != OK) return Collections.emptyList();

    Object[] next = new Object[iterators.length];
    for (int i = 0; i < iterators.length; i++)
      try {
        next[i] = iterators[i].next();
      } catch (Throwable t) {
        runStatus = supervisor.error(
            new ErrorInfo(currentFeature.getDataProviders().get(i).getDataProviderMethod(), t));
        return Collections.emptyList();
      }

    try {
      return Arrays.asList((Object[]) invokeRaw(sharedInstance, currentFeature.getDataProcessorMethod(), next));
    } catch (Throwable t) {
      runStatus = supervisor.error(
          new ErrorInfo(currentFeature.getDataProcessorMethod(), t));
      return Collections.emptyList();
    }
  }
}
