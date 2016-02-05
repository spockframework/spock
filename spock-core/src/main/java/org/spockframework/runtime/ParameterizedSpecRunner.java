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

import java.util.ArrayList;
import java.util.*;

import static java.util.Arrays.*;
import static org.spockframework.runtime.RunStatus.*;

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
    runIterations(dataProviders);
    closeDataProviders(dataProviders);
  }

  private List<Object> createDataProviders() {
    if (runStatus != OK) return null;

    List<Object> dataProviders = new ArrayList<Object>();
    for (DataProviderInfo dataProviderInfo : currentFeature.getDataProviders()) {
      MethodInfo method = dataProviderInfo.getDataProviderMethod();
      Object[] arguments = copyOf(dataProviders.toArray(), getDataTableOffset(dataProviderInfo));
      Object provider = invokeRaw(sharedInstance, method, arguments);

      if (runStatus != OK)
        return null;
      else if (provider == null) {
        SpockExecutionException error = new SpockExecutionException("Data provider is null!");
        runStatus = supervisor.error(new ErrorInfo(method, error));
        return null;
      }
      dataProviders.add(provider);
    }

    return dataProviders;
  }

  /* TODO remove when all *where* elements are data providers */
  private int getDataTableOffset(DataProviderInfo dataProviderInfo) {
    int result = 0;
    for (String variableName : dataProviderInfo.getDataVariables()) {
      for (String parameterName : currentFeature.getParameterNames()) {
        if (variableName.equals(parameterName))
          return result;
        else
          result++;
      }
    }
    throw new IllegalStateException(String.format("Variable name not defined (%s not in %s)!",
                                                  dataProviderInfo.getDataVariables(),
                                                  currentFeature.getParameterNames()));
  }

  private List<Iterator> createIterators(List<Object> dataProviders) {
    if (runStatus != OK) return null;

    List<Iterator> iterators = new ArrayList<Iterator>();
    for (int i = 0; i < dataProviders.size(); i++)
      try {
        Iterator<?> iter = GroovyRuntimeUtil.asIterator(dataProviders.get(i));
        if (iter == null) {
          runStatus = supervisor.error(new ErrorInfo(currentFeature.getDataProviders().get(i).getDataProviderMethod(),
                                                     new SpockExecutionException("Data provider's iterator() method returned null")));
          return null;
        }
        iterators.add(iter);
      } catch (RuntimeException e) {
        runStatus = supervisor.error(new ErrorInfo(currentFeature.getDataProviders().get(i).getDataProviderMethod(), e));
        return null;
      }

    return iterators;
  }

  private void runIterations(List<Object> dataProviders) {
    if (runStatus != OK) return;

    for (List<Iterator> iterators = createIterators(dataProviders); haveNext(iterators); ) {
      initializeAndRunIteration(nextArgs(iterators));

      if (resetStatus(ITERATION) != OK) break;
      // no iterators => no data providers => only derived parameterizations => limit to one iteration
      if (iterators.isEmpty()) break;
    }
  }

  private void closeDataProviders(List<Object> dataProviders) {
    if ((action(runStatus) != OK) || (dataProviders == null)) return;

    for (Object provider : dataProviders)
      GroovyRuntimeUtil.invokeMethodQuietly(provider, "close");
  }

  private boolean haveNext(List<Iterator> iterators) {
    if (runStatus != OK) return false;

    boolean haveNext = true;

    for (int i = 0; i < iterators.size(); i++)
      try {
        boolean hasNext = iterators.get(i).hasNext();
        if (i == 0) haveNext = hasNext;
        else if (haveNext != hasNext) {
          DataProviderInfo provider = currentFeature.getDataProviders().get(i);
          runStatus = supervisor.error(new ErrorInfo(provider.getDataProviderMethod(),
                                                     createDifferentNumberOfDataValuesException(provider, hasNext)));
          return false;
        }
      } catch (RuntimeException e) {
        runStatus = supervisor.error(new ErrorInfo(currentFeature.getDataProviders().get(i).getDataProviderMethod(), e));
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
  private List<Object> nextArgs(List<Iterator> iterators) {
    if (runStatus != OK) return null;

    List<Object> next = new ArrayList<Object>();
    for (int i = 0; i < iterators.size(); i++)
      try {
        next.add(iterators.get(i).next());
      } catch (RuntimeException e) {
        runStatus = supervisor.error(new ErrorInfo(currentFeature.getDataProviders().get(i).getDataProviderMethod(), e));
        return null;
      }

    try {
      return asList((Object[]) invokeRaw(sharedInstance, currentFeature.getDataProcessorMethod(), next.toArray()));
    } catch (RuntimeException e) {
      runStatus = supervisor.error(new ErrorInfo(currentFeature.getDataProcessorMethod(), e));
      return null;
    }
  }
}
