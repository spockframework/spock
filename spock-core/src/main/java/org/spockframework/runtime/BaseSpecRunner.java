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

import static org.spockframework.runtime.RunStatus.*;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.MethodInvocation;
import org.spockframework.runtime.model.*;
import org.spockframework.util.InternalSpockError;
import org.spockframework.util.ReflectionUtil;

/**
 * Executes a single Spec. Notifies its supervisor about overall execution
 * progress and every invocation of Spec code.
 * Supervisor also determines the error strategy.
 *
 * @author Peter Niederwieser
 */
public class BaseSpecRunner {
  private static final Method DO_RUN;
  private static final Method DO_RUN_FEATURE;

  protected final SpecInfo spec;
  protected final IRunSupervisor supervisor;

  protected FeatureInfo currentFeature;

  protected Object sharedInstance;
  protected Object currentInstance;
  protected int runStatus = OK;

  static {
    try {
      DO_RUN = BaseSpecRunner.class.getMethod("doRun");
      DO_RUN_FEATURE = BaseSpecRunner.class.getMethod("doRunFeature");
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

    supervisor.beforeSpec(spec);
    invoke(this, createDoRunInfo());
    supervisor.afterSpec(spec);

    return resetStatus(SPEC);
  }

  private MethodInfo createDoRunInfo() {
    MethodInfo result = new MethodInfo();
    result.setParent(spec);
    result.setKind(MethodKind.SPEC_EXECUTION);
    result.setReflection(DO_RUN);
    result.setDescription(spec.getDescription());
    for (IMethodInterceptor interceptor : spec.getInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  /**
   * Only called via reflection.
   */
  public void doRun() {
    createSpecInstance(true);
    invokeSetupSpec();
    runFeatures();
    invokeCleanupSpec();
  }

  protected void createSpecInstance(boolean shared) {
    if (runStatus != OK) return;

    try {
      if (shared) {
        sharedInstance = spec.getReflection().newInstance();
        // make sure that x.getOrSetSomeSharedField() also works for x == sharedInstance
        // (important for setupSpec/cleanupSpec)
        spec.getSharedInstanceField().getReflection().set(sharedInstance, sharedInstance);
      } else {
        currentInstance = spec.getReflection().newInstance();
        spec.getSharedInstanceField().getReflection().set(currentInstance, sharedInstance);
      }
    } catch (Throwable t) {
      throw new InternalSpockError("Failed to instantiate spec '%s'", t).withArgs(spec.getName());
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
    invoke(this, createDoRunFeatureInfo());
    supervisor.afterFeature(currentFeature);
  }

  private MethodInfo createDoRunFeatureInfo() {
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
  public void doRunFeature() {
    if (currentFeature.isParameterized())
      runParameterizedFeature();
    else runSimpleFeature();
  }

  private void runSimpleFeature() {
    if (runStatus != OK) return;

    createSpecInstance(false);
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

  protected void invokeSetup() {
    for (SpecInfo curr : spec.getSpecsTopToBottom()) {
      if (runStatus != OK) return;
      invoke(currentInstance, curr.getSetupMethod());
    }
  }

  protected void invokeFeatureMethod(Object... args) {
    if (runStatus != OK) return;
    invoke(currentInstance, currentFeature.getFeatureMethod(), args);
  }

  protected void invokeCleanup() {
    for (SpecInfo curr : spec.getSpecsBottomToTop()) {
      if (action(runStatus) == ABORT) return;
      invoke(currentInstance, curr.getCleanupMethod());
    }
  }

  protected void invoke(Object target, MethodInfo method, Object... arguments) {
    // fast lane
    if (method.getInterceptors().isEmpty()) {
      invokeRaw(target, method, arguments);
      return;
    }

    // slow lane
    MethodInvocation invocation = new MethodInvocation(currentFeature, target, method, arguments);
    try {
      invocation.proceed();
    } catch (Throwable t) {
      ErrorInfo error = new ErrorInfo(method, t);
      runStatus = supervisor.error(error);
    }
  }

  protected Object invokeRaw(Object target, MethodInfo method, Object[] arguments) {
    if (method.isStub()) return null;

    try {
      return ReflectionUtil.invokeMethod(target, method.getReflection(), arguments);
    } catch (Throwable t) {
      runStatus = supervisor.error(new ErrorInfo(method, t));
      return null;
    }
  }
}

