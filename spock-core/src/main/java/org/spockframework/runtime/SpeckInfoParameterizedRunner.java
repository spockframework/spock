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

import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.runtime.InvokerHelper;

import static org.spockframework.runtime.RunStatus.*;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.SpeckInfo;

/**
 * Adds the ability to run parameterized features.
 *
 * @author Peter Niederwieser
 */
public class SpeckInfoParameterizedRunner extends SpeckInfoBaseRunner {
  public SpeckInfoParameterizedRunner(SpeckInfo speck, IRunSupervisor supervisor) {
    super(speck, supervisor);
  }

  @Override
  protected void runParameterizedFeature(MethodInfo feature) {
    if (runStatus != OK) return;

    Object[] dataProviders = createDataProviders(feature);
    int numIterations = estimateNumIterations(dataProviders);
    Iterator[] iterators = createIterators(feature, dataProviders);
    runIterations(feature, iterators, numIterations);
    closeDataProviders(dataProviders);
  }

  private Object[] createDataProviders(MethodInfo feature) {
    if (runStatus != OK) return null;

    List<MethodInfo> dataProviderMethods = feature.getDataProviders();
    Object[] dataProviders = new Object[dataProviderMethods.size()];

    for (int i = 0; i < dataProviderMethods.size(); i++) {
      MethodInfo method = dataProviderMethods.get(i);
      Object provider = invokeRaw(method, null);
      if (runStatus != OK) return null;
      if (provider == null) {
        runStatus = supervisor.error(method, new SpeckExecutionException("Data provider is null"), runStatus);
        return null;
      }
      dataProviders[i] = provider;
    }

    return dataProviders;
  }

  private Iterator[] createIterators(MethodInfo feature, Object[] dataProviders) {
    if (runStatus != OK) return null;

    Iterator[] iterators = new Iterator[dataProviders.length];
    for (int i = 0; i < dataProviders.length; i++)
      try {
        Iterator iter = InvokerHelper.asIterator(dataProviders[i]);
        if (iter == null) {
          runStatus = supervisor.error(feature.getDataProviders().get(i),
              new SpeckExecutionException("Data provider's iterator() method returned null"), runStatus);
          return null;
        }
        iterators[i] = iter;
      } catch (Throwable t) {
        runStatus = supervisor.error(feature.getDataProviders().get(i), t, runStatus);
        return null;
      }

    return iterators;
  }

  // TODO: check whether in Groovy, size() always returns result that is consistent with iterator()
  // if not, handle divergent cases (File is probably already a counter example)
  // -1 => unknown
  private int estimateNumIterations(Object[] dataProviders) {
    if (runStatus != OK) return -1;
    if (dataProviders.length == 0) return 1;

    int result = Integer.MAX_VALUE;
    for (Object prov : dataProviders) {
      if (prov instanceof Iterator)
        // unbelievably, DGM provides a size() method for Iterators,
        // although it is of course destructive (i.e. exhausts the Iterator)
        continue;
      try {
        int size = (Integer)InvokerHelper.invokeMethod(prov, "size", null);
        if (size >= 0 && size < result) result = size;
      } catch (Throwable ignored) {}
    }

    return result == Integer.MAX_VALUE ? -1 : result;
  }

  private void runIterations(MethodInfo feature, Iterator[] iterators, int estimatedNumIterations) {
    if (runStatus != OK) return;

    supervisor.beforeFirstIteration(estimatedNumIterations);

    while (haveNext(feature, iterators)) {
      Object[] args = nextArgs(feature, iterators);
      runIteration(feature, args);
      
      if (runStatus != OK) break;
      // no iterators => no data providers => only derived parameterizations => limit to one iteration
      if(iterators.length == 0) break;
    }

    supervisor.afterLastIteration();
  }

  private void closeDataProviders(Object[] dataProviders) {
    if (action(runStatus) == ABORT) return;

    for (Object provider : dataProviders) {
      // check whether provider has been created successfully
      // if not, also subsequent providers haven't been created
      if (provider == null) return;
      try {
        InvokerHelper.invokeMethod(provider, "close", null);
      } catch (Throwable ignored) {}
    }
  }

  private void runIteration(MethodInfo feature, Object[] args) {
    if (runStatus != OK) return;

    supervisor.beforeIteration();
    invokeSetup();
    invokeFeature(feature, args);
    invokeCleanup();
    supervisor.afterIteration();
    resetStatus(ITERATION);
  }

  private boolean haveNext(MethodInfo feature, Iterator[] iterators) {
    if (runStatus != OK) return false;

    for (int i = 0; i < iterators.length; i++)
      try {
        if (!iterators[i].hasNext()) return false;
      } catch (Throwable t) {
        runStatus = supervisor.error(feature.getDataProviders().get(i), t, runStatus);
        return false;
      }

    return true;
  }

  // advances iterators and computes args
  private Object[] nextArgs(MethodInfo feature, Iterator[] iterators) {
    if (runStatus != OK) return null;

    Object[] next = new Object[iterators.length];
    for (int i = 0; i < iterators.length; i++)
      try {
        next[i] = iterators[i].next();
      } catch (Throwable t) {
        runStatus = supervisor.error(feature.getDataProviders().get(i), t, runStatus);
        return null;
      }

    try {
      return (Object[])invokeRaw(feature.getDataProcessor(), next);
    } catch (Throwable t) {
      runStatus = supervisor.error(feature.getDataProcessor(), t, runStatus);
      return null;
    }
  }
}
