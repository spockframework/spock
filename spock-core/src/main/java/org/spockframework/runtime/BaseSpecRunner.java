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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.spockframework.runtime.RunStatus.*;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.MethodInvocation;
import org.spockframework.runtime.model.*;
import org.spockframework.util.Assert;
import org.spockframework.util.InternalSpockError;

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
  protected Object sharedInstance;
  protected Object currentInstance;
  protected final IRunSupervisor supervisor;
  protected int runStatus = OK;

  static {
    try {
      DO_RUN = BaseSpecRunner.class.getMethod("doRun");
      DO_RUN_FEATURE = BaseSpecRunner.class.getMethod("doRunFeature", FeatureInfo.class);
    } catch (NoSuchMethodException e) {
      throw new InternalSpockError(e);
    }
  }

  public BaseSpecRunner(SpecInfo spec, IRunSupervisor supervisor) {
    this.spec = spec;
    this.supervisor = supervisor;
  }

  public int run() {
    Assert.that(!spec.isExcluded(), "tried to run excluded spec");
    if (spec.isSkipped()) {
      supervisor.specSkipped(spec);
      return OK;
    }

    supervisor.beforeSpec(spec);
    invoke(this, createDoRunInfo());
    supervisor.afterSpec();

    return resetStatus(SPEC);
  }

  private MethodInfo createDoRunInfo() {
    MethodInfo result = new MethodInfo();
    result.setParent(spec);
    result.setKind(MethodKind.SPEC_EXECUTION);
    result.setReflection(DO_RUN);
    result.setMetadata(spec.getMetadata());
    for (IMethodInterceptor interceptor : spec.getInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  /**
   * Only called via reflection.
   */
  public void doRun() {
    createSpecInstance(true);
    invokeSetupSpec(spec);
    runFeatures(spec);
    invokeCleanupSpec(spec);
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

  private void invokeSetupSpec(SpecInfo spec) {
    if (spec == null) return;
    invokeSetupSpec(spec.getSuperSpec());
    if (runStatus != OK) return;
    invoke(sharedInstance, spec.getSetupSpecMethod());
  }

  private void runFeatures(SpecInfo spec) {
    if (spec == null) return;
    runFeatures(spec.getSuperSpec());
    if (runStatus != OK) return;

    for (FeatureInfo feature : spec.getFeatures()) {
      runFeature(feature);
      if (resetStatus(FEATURE) != OK) return;
    }
  }

  private void invokeCleanupSpec(SpecInfo spec) {
    if (spec == null) return;
    invoke(sharedInstance, spec.getCleanupSpecMethod());
    if (action(runStatus) == ABORT) return;
    invokeCleanupSpec(spec.getSuperSpec());
  }

  private void runFeature(FeatureInfo feature) {
    if (runStatus != OK) return;

    Assert.that(!feature.isExcluded(), "tried to run excluded feature");
    if (feature.isSkipped()) {
      supervisor.featureSkipped(feature);
      return;
    }

    supervisor.beforeFeature(feature);
    invoke(this, createDoRunFeatureInfo(feature), feature);
    supervisor.afterFeature();
  }

  private MethodInfo createDoRunFeatureInfo(FeatureInfo feature) {
    MethodInfo result = new MethodInfo();
    result.setParent(feature.getParent());
    result.setKind(MethodKind.FEATURE_EXECUTION);
    result.setReflection(DO_RUN_FEATURE);
    result.setFeature(feature);
    result.setMetadata(feature.getMetadata());
    for (IMethodInterceptor interceptor : feature.getInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  /**
   * Only called via reflection.
   *
   * @param feature the feature method to be run
   */
  public void doRunFeature(FeatureInfo feature) {
    if (feature.isParameterized())
      runParameterizedFeature(feature);
    else runSimpleFeature(feature);
  }

  private void runSimpleFeature(FeatureInfo feature) {
    if (runStatus != OK) return;

    createSpecInstance(false);
    invokeSetup(spec);
    invokeFeatureMethod(feature.getFeatureMethod());
    invokeCleanup(spec);
  }

  protected int resetStatus(int scope) {
    if (scope(runStatus) <= scope) runStatus = OK;
    return runStatus;
  }

  protected void runParameterizedFeature(FeatureInfo feature) {
    throw new UnsupportedOperationException("This runner cannot run parameterized features");
  }

  protected void invokeSetup(SpecInfo spec) {
    if (spec == null) return;
    invokeSetup(spec.getSuperSpec());
    if (runStatus != OK) return;
    invoke(spec.getSetupMethod());
  }

  protected void invokeFeatureMethod(MethodInfo feature, Object... args) {
    if (runStatus != OK) return;
    invoke(feature, args);
  }

  protected void invokeCleanup(SpecInfo spec) {
    if (spec == null) return;
    invoke(spec.getCleanupMethod());
    if (action(runStatus) == ABORT) return;
    invokeCleanup(spec.getSuperSpec());
  }

  protected void invoke(Object target, MethodInfo method, Object... arguments) {
    // fast lane
    if (method.getInterceptors().isEmpty()) {
      invokeRaw(target, method, arguments);
      return;
    }

    // slow lane
    MethodInvocation invocation = new MethodInvocation(target, method, arguments);
    try {
      invocation.proceed();
    } catch (Throwable t) {
      runStatus = supervisor.error(method, t, runStatus);
    }
  }

  protected void invoke(MethodInfo method, Object... arguments) {
    invoke(currentInstance, method, arguments);
  }

  protected Object invokeRaw(Object target, MethodInfo method, Object[] arguments) {
    if (method.isStub()) return null;

    try {
      return method.getReflection().invoke(target, arguments);
    } catch (InvocationTargetException e) {
      runStatus = supervisor.error(method, e.getTargetException(), runStatus);
      return null;
    } catch (Throwable t) {
      Error internalError =
          new InternalSpockError("Failed to invoke method '%s'", t).withArgs(method.getReflection().getName());
      runStatus = supervisor.error(method, internalError, runStatus);
      return null;
    }
  }

  protected Object invokeRaw(MethodInfo method, Object[] arguments) {
    return invokeRaw(currentInstance, method, arguments);
  }
}

