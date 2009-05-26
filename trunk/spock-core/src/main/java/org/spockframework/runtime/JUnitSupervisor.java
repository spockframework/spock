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

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import static org.spockframework.runtime.RunStatus.END_FEATURE;
import static org.spockframework.runtime.RunStatus.END_SPECK;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.SpeckInfo;
import org.spockframework.runtime.model.FeatureInfo;

// IDEA: represent setupSpeck()/afterSpeck() as JUnit test methods
public class JUnitSupervisor implements IRunSupervisor {
  private final RunNotifier notifier;
  private SpeckInfo speck;
  private MethodInfo featureMethod;
  private int numIterations;

  private StackTraceFilter filter;

  public JUnitSupervisor(RunNotifier notifier) {
    this.notifier = notifier;
  }

  public void beforeSpeck(SpeckInfo speck) {
    this.speck = speck;
    this.filter = new StackTraceFilter(speck);
  }

  public void beforeFeature(FeatureInfo feature) {
    this.featureMethod = feature.getFeatureMethod();
    notifier.fireTestStarted(getDescription(feature.getFeatureMethod()));
  }

  public void beforeFirstIteration(int estimatedNumIterations) {
    numIterations = 0;
  }

  public void beforeIteration() {
    numIterations++;
  }

  public int error(MethodInfo method, Throwable throwable, int runStatus) {
    filter.filter(throwable);

    Description description = getFailureDescription(featureMethod, method);
    if (throwable instanceof SkipSpeckOrFeatureException) {
      notifier.fireTestIgnored(description);
      return END_FEATURE;
    }
    
    notifier.fireTestFailure(new Failure(description, throwable));

    switch (method.getKind()) {
      case FEATURE_EXECUTION:
      case FEATURE:
      case DATA_PROVIDER:
      case DATA_PROCESSOR:
        return END_FEATURE;
      default:
        return END_SPECK;
    }
  }

  public void afterIteration() {}

  public void afterLastIteration() {
    if (numIterations == 0)
      notifier.fireTestFailure(new Failure(getDescription(featureMethod),
          new SpeckExecutionException("Data provider has no data")));
  }
  
  public void afterFeature() {
    notifier.fireTestFinished(getDescription(featureMethod));
    featureMethod = null;
  }

  public void afterSpeck() {}

  private Description getDescription(MethodInfo method) {
    return (Description)method.getMetadata();
  }

  private Description getFailureDescription(MethodInfo currentFeature, MethodInfo failedMethod) {
    return getDescription(currentFeature == null ? failedMethod : currentFeature);
  }
}
