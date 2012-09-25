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

import java.lang.reflect.Method;

import org.junit.runner.Description;

import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.MethodInvocation;
import org.spockframework.runtime.model.*;
import org.spockframework.util.InternalSpockError;
import org.spockframework.util.ReflectionUtil;
import spock.lang.Specification;

import static org.spockframework.runtime.RunStatus.*;

/**
 * Executes a single Spec. Notifies its supervisor about overall execution
 * progress and every invocation of Spec code.
 * Supervisor also determines the error strategy.
 *
 * @author Peter Niederwieser
 */
public class BaseSpecRunner {
  private static final Method DO_RUN_SPEC;
  private static final Method DO_RUN_FEATURE;
  private static final Method DO_RUN_ITERATION;

  protected static final Object[] EMPTY_ARGS = new Object[0];

  protected final SpecInfo spec;
  protected final IRunSupervisor supervisor;

  protected FeatureInfo currentFeature;
  protected IterationInfo currentIteration;

  protected Specification sharedInstance;
  protected Specification currentInstance;
  protected int runStatus = OK;

  static {
    try {
      DO_RUN_SPEC = BaseSpecRunner.class.getMethod("doRunSpec");
      DO_RUN_FEATURE = BaseSpecRunner.class.getMethod("doRunFeature");
      DO_RUN_ITERATION = BaseSpecRunner.class.getMethod("doRunIteration");
    } catch (NoSuchMethodException e) {
      throw new InternalSpockError(e);
    }
  }

  public BaseSpecRunner(SpecInfo spec, IRunSupervisor supervisor) {
    this.spec = spec;
    this.supervisor = supervisor;
  }

  public int run() {
    // Sometimes a spec run is requested even though the spec has been excluded
    // (e.g. if JUnit is in control). In such a case, the best thing we can do
    // is to treat the spec as skipped.
    if (spec.isExcluded() || spec.isSkipped()) {
      supervisor.specSkipped(spec);
      return OK;
    }

    createSpecInstance(true);
    invokeSharedInitializer();
    runSpec();

    return resetStatus(SPEC);
  }

  private void runSpec() {
    if (runStatus != OK) return;

    supervisor.beforeSpec(spec);
    invoke(this, createMethodInfoForDoRunSpec());
    supervisor.afterSpec(spec);
  }

  private MethodInfo createMethodInfoForDoRunSpec() {
    MethodInfo result = new MethodInfo();
    result.setParent(spec);
    result.setKind(MethodKind.SPEC_EXECUTION);
    result.setReflection(DO_RUN_SPEC);
    result.setDescription(spec.getDescription());
    for (IMethodInterceptor interceptor : spec.getInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  /**
   * Only called via reflection.
   */
  @SuppressWarnings("unused")
  public void doRunSpec() {
    invokeSetupSpec();
    runFeatures();
    invokeCleanupSpec();
  }

  private void createSpecInstance(boolean shared) {
    if (runStatus != OK) return;

    try {
      if (shared) {
        sharedInstance = (Specification) spec.getReflection().newInstance();
        // make sure that x.getOrSetSomeSharedField() also works for x == sharedInstance
        // (important for setupSpec/cleanupSpec)
        getSpecificationContext(sharedInstance).setSharedInstance(sharedInstance);
      } else {
        currentInstance = (Specification) spec.getReflection().newInstance();
        getSpecificationContext(currentInstance).setSharedInstance(sharedInstance);
      }
    } catch (Throwable t) {
      throw new InternalSpockError("Failed to instantiate spec '%s'", t).withArgs(spec.getName());
    }
  }

  private void invokeSharedInitializer() {
    for (SpecInfo curr : spec.getSpecsTopToBottom()) {
      if (runStatus != OK) return;
      invoke(sharedInstance, curr.getSharedInitializerMethod());
    }
  }

  private void invokeSetupSpec() {
    for (SpecInfo curr : spec.getSpecsTopToBottom()) {
      if (runStatus != OK) return;
      invoke(sharedInstance, curr.getSetupSpecMethod());
    }
  }

  private void runFeatures() {
    for (FeatureInfo feature : spec.getAllFeaturesInExecutionOrder()) {
      if (resetStatus(FEATURE) != OK) return;
      currentFeature = feature;
      runFeature();
      currentFeature = null;
    }
  }

  private void invokeCleanupSpec() {
    for (SpecInfo curr : spec.getSpecsBottomToTop()) {
      if (action(runStatus) == ABORT) return;
      invoke(sharedInstance, curr.getCleanupSpecMethod());
    }
  }

  private void runFeature() {
    if (runStatus != OK) return;

    if (currentFeature.isExcluded()) return;

    if (currentFeature.isSkipped()) {
      supervisor.featureSkipped(currentFeature);
      return;
    }

    supervisor.beforeFeature(currentFeature);
    invoke(this, createMethodInfoForDoRunFeature());
    supervisor.afterFeature(currentFeature);
  }

  private MethodInfo createMethodInfoForDoRunFeature() {
    MethodInfo result = new MethodInfo();
    result.setParent(currentFeature.getParent());
    result.setKind(MethodKind.FEATURE_EXECUTION);
    result.setReflection(DO_RUN_FEATURE);
    result.setFeature(currentFeature);
    result.setDescription(currentFeature.getDescription());
    for (IMethodInterceptor interceptor : currentFeature.getInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  /**
   * Only called via reflection.
   */
  @SuppressWarnings("unused")
  public void doRunFeature() {
    currentFeature.setIterationNameProvider(new SafeIterationNameProvider(currentFeature.getIterationNameProvider()));
    if (currentFeature.isParameterized())
      runParameterizedFeature();
    else runSimpleFeature();
  }

  private void runSimpleFeature() {
    if (runStatus != OK) return;

    initializeAndRunIteration(EMPTY_ARGS, 1);
    resetStatus(ITERATION);
  }

  protected void initializeAndRunIteration(Object[] dataValues, int estimatedNumIterations) {
    if (runStatus != OK) return;

    createSpecInstance(false);
    invokeInitializer();
    runIteration(dataValues, estimatedNumIterations);
  }

  private void runIteration(Object[] dataValues, int estimatedNumIterations) {
    if (runStatus != OK) return;

    currentIteration = createIterationInfo(dataValues, estimatedNumIterations);
    getSpecificationContext(currentInstance).setIterationInfo(currentIteration);
    supervisor.beforeIteration(currentIteration);
    invoke(this, createMethodInfoForDoRunIteration());
    supervisor.afterIteration(currentIteration);
    currentIteration = null;
  }

  private IterationInfo createIterationInfo(Object[] dataValues, int estimatedNumIterations) {
    currentIteration = new IterationInfo(currentFeature, dataValues, estimatedNumIterations);
    String iterationName = currentFeature.getIterationNameProvider().getName(currentIteration);
    currentIteration.setName(iterationName);
    Description description = Description.createTestDescription(spec.getReflection(),
        iterationName, currentFeature.getFeatureMethod().getReflection().getAnnotations());
    currentIteration.setDescription(description);
    return currentIteration;
  }

  private MethodInfo createMethodInfoForDoRunIteration() {
    MethodInfo result = new MethodInfo();
    result.setParent(currentFeature.getParent());
    result.setKind(MethodKind.ITERATION_EXECUTION);
    result.setReflection(DO_RUN_ITERATION);
    result.setFeature(currentFeature);
    result.setDescription(currentFeature.getDescription());
    for (IMethodInterceptor interceptor : currentFeature.getIterationInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  /**
   * Only called via reflection.
   */
  @SuppressWarnings("unused")
  public void doRunIteration() {
    invokeSetup();
    invokeFeatureMethod();
    invokeCleanup();
  }

  protected int resetStatus(int scope) {
    if (scope(runStatus) <= scope) runStatus = OK;
    return runStatus;
  }

  protected void runParameterizedFeature() {
    throw new UnsupportedOperationException("This runner cannot run parameterized features");
  }

  private void invokeInitializer() {
    for (SpecInfo curr : spec.getSpecsTopToBottom()) {
      if (runStatus != OK) return;
      invoke(currentInstance, curr.getInitializerMethod());
    }
  }

  private void invokeSetup() {
    for (SpecInfo curr : spec.getSpecsTopToBottom()) {
      if (runStatus != OK) return;
      invoke(currentInstance, curr.getSetupMethod());
    }
  }

  private void invokeFeatureMethod() {
    if (runStatus != OK) return;
    invoke(currentInstance, currentFeature.getFeatureMethod(), currentIteration.getDataValues());
  }

  private void invokeCleanup() {
    invokeIterationCleanups();
    for (SpecInfo curr : spec.getSpecsBottomToTop()) {
      if (action(runStatus) == ABORT) return;
      invoke(currentInstance, curr.getCleanupMethod());
    }
  }

  private void invokeIterationCleanups() {
    for (Runnable cleanup : currentIteration.getCleanups()) {
      if (action(runStatus) == ABORT) return;
      try {
        cleanup.run();
      } catch (Throwable t) {
        ErrorInfo error = new ErrorInfo(spec.getCleanupMethod(), t);
        runStatus = supervisor.error(error);
      }
    }
  }

  private void invoke(Object target, MethodInfo method, Object... arguments) {
    // fast lane
    if (method.getInterceptors().isEmpty()) {
      invokeRaw(target, method, arguments);
      return;
    }

    // slow lane
    MethodInvocation invocation = new MethodInvocation(currentFeature,
        currentIteration, sharedInstance, currentInstance, target, method, arguments);
    try {
      invocation.proceed();
    } catch (Throwable t) {
      ErrorInfo error = new ErrorInfo(method, t);
      runStatus = supervisor.error(error);
    }
  }

  protected Object invokeRaw(Object target, MethodInfo method, Object... arguments) {
    if (method.isStub()) return null;

    try {
      return ReflectionUtil.invokeMethod(target, method.getReflection(), arguments);
    } catch (Throwable t) {
      runStatus = supervisor.error(new ErrorInfo(method, t));
      return null;
    }
  }

  protected SpecificationContext getSpecificationContext(Specification instance) {
    return (SpecificationContext) instance.getSpecificationContext();
  }
}

