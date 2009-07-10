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

import static org.spockframework.runtime.RunStatus.*;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.SpeckInfo;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.util.InternalSpockError;
import spock.lang.*;

// IDEA: represent setupSpeck()/afterSpeck() as JUnit test methods
public class JUnitSupervisor implements IRunSupervisor {
  private final RunNotifier notifier;
  private SpeckInfo speck;
  private StackTraceFilter filter;

  private FeatureInfo feature;
  private boolean ignoredFeature;
  private boolean unrollFeature;
  private UnrolledFeatureNameGenerator unrolledNameGenerator;

  private int iterationCount;
  private Description unrolledDescription;
  private boolean errorSinceLastReset;

  public JUnitSupervisor(RunNotifier notifier) {
    this.notifier = notifier;
  }

  public void beforeSpeck(SpeckInfo speck) {
    this.speck = speck;
    this.filter = new StackTraceFilter(speck);
  }

  public void beforeFeature(FeatureInfo feature) {
    this.feature = feature;

    // make sure we don't call fireTestStarted/fireTestFinished for ignored features
    ignoredFeature = feature.getFeatureMethod().getReflection().getAnnotation(Ignore.class) != null;
    if (ignoredFeature) return;

    Unroll unroll = feature.getFeatureMethod().getReflection().getAnnotation(Unroll.class);
    unrollFeature = unroll != null;
    if (unrollFeature)
      unrolledNameGenerator = new UnrolledFeatureNameGenerator(feature, unroll);
    else
      notifier.fireTestStarted(getDescription(feature.getFeatureMethod()));
  }

  public void beforeFirstIteration(int estimatedNumIterations) {
    iterationCount = 0;
    errorSinceLastReset = false;
  }

  public void beforeIteration(Object[] args) {
    iterationCount++;
    if (!unrollFeature) return;

    unrolledDescription = getUnrolledDescription(args);
    notifier.fireTestStarted(unrolledDescription);
  }

  public int error(MethodInfo method, Throwable throwable, int runStatus) {
    errorSinceLastReset = true;
    filter.filter(throwable);

    Description description = getFailedDescription(method);
    if (throwable instanceof SkipSpeckOrFeatureException) {
      notifier.fireTestIgnored(description);
      return OK;
    }
    
    notifier.fireTestFailure(new Failure(description, throwable));

    switch (method.getKind()) {
      case DATA_PROCESSOR:
        return END_ITERATION;
      case SETUP:
      case CLEANUP:     
      case FEATURE:
        return feature.isParameterized() ? END_ITERATION : END_FEATURE;
      case FEATURE_EXECUTION:
      case DATA_PROVIDER:
        return END_FEATURE;
      case SETUP_SPECK:
      case CLEANUP_SPECK:
      case SPECK_EXECUTION:
        return END_SPECK;
      default:
        throw new InternalSpockError(method.getKind().toString());
    }
  }

  public void afterIteration() {
    if (!unrollFeature) return;

    notifier.fireTestFinished(unrolledDescription);
    unrolledDescription = null;
  }

  public void afterLastIteration() {
    if (iterationCount == 0 && !errorSinceLastReset)
      notifier.fireTestFailure(new Failure(getDescription(feature.getFeatureMethod()),
          new SpeckExecutionException("Data provider has no data")));
  }
  
  public void afterFeature() {
    if (!(ignoredFeature || unrollFeature))
      notifier.fireTestFinished(getDescription(feature.getFeatureMethod()));

    feature = null;
    ignoredFeature = false;
    unrollFeature = false;
    unrolledNameGenerator = null;
  }

  public void afterSpeck() {}

  private Description getDescription(MethodInfo method) {
    return (Description)method.getMetadata();
  }

  private Description getFailedDescription(MethodInfo failedMethod) {
    if (unrolledDescription != null) return unrolledDescription;
    if (feature != null) return getDescription(feature.getFeatureMethod());
    return getDescription(failedMethod);
  }

  private Description getUnrolledDescription(Object[] args) {
    return Description.createTestDescription(speck.getReflection(), unrolledNameGenerator.nameFor(args));
  }
}
