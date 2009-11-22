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
import org.spockframework.runtime.model.*;
import org.spockframework.util.InternalSpockError;

import spock.lang.Ignore;
import spock.lang.Unroll;

public class JUnitSupervisor implements IRunSupervisor {
  private final RunNotifier notifier;
  private SpecInfo spec;
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

  public void beforeSpec(SpecInfo spec) {
    this.spec = spec;
    this.filter = new StackTraceFilter(spec);
  }

  public void beforeFeature(FeatureInfo feature) {
    this.feature = feature;

    // make sure we don't call fireTestStarted/fireTestFinished for ignored features
    // a bit of a hack; for example, what will happen if exception is thrown before
    // IgnoreInterceptor gets to throw its SkipSpecOrFeatureException?
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
    if (throwable instanceof SkipSpecOrFeatureException) {
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
      case SETUP_SPEC:
      case CLEANUP_SPEC:
      case SPEC_EXECUTION:
        return END_SPEC;
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
          new SpockExecutionException("Data provider has no data")));
  }
  
  public void afterFeature() {
    if (!(ignoredFeature || unrollFeature))
      notifier.fireTestFinished(getDescription(feature.getFeatureMethod()));

    feature = null;
    ignoredFeature = false;
    unrollFeature = false;
    unrolledNameGenerator = null;
  }

  public void afterSpec() {}

  private Description getDescription(NodeInfo node) {
    return (Description)node.getMetadata();
  }

  private Description getFailedDescription(MethodInfo failedMethod) {
    if (unrolledDescription != null) return unrolledDescription;
    if (feature != null) return getDescription(feature.getFeatureMethod());
    // attribute failure to Spec
    // if we would attribute failure to a method that isn't part of the Spec's
    // static Description, IDEs would fail to report it correctly
    return getDescription(spec);
  }

  private Description getUnrolledDescription(Object[] args) {
    return Description.createTestDescription(spec.getReflection(), unrolledNameGenerator.nameFor(args));
  }
}
