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

import static org.spockframework.runtime.RunStatus.*;
import org.spockframework.runtime.intercept.IMethodInterceptor;
import org.spockframework.runtime.intercept.MethodInvocation;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.MethodKind;
import org.spockframework.runtime.model.SpeckInfo;
import org.spockframework.util.InternalSpockError;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Executes a single Speck. Notifies its supervisor about overall execution
 * progress and every invocation of Speck code.
 * Supervisor also determines the error strategy.
 *
 * @author Peter Niederwieser
 */
public class BaseSpeckRunner {
  private static final Method DO_RUN;
  private static final Method DO_RUN_FEATURE;

  protected final SpeckInfo speck;
  protected Object sharedInstance;
  protected Object currentInstance;
  protected final IRunSupervisor supervisor;
  protected int runStatus = OK;

  static {
    try {
      DO_RUN = BaseSpeckRunner.class.getMethod("doRun");
      DO_RUN_FEATURE = BaseSpeckRunner.class.getMethod("doRunFeature", FeatureInfo.class);
    } catch (NoSuchMethodException e) {
      throw new InternalSpockError();
    }
  }

  public BaseSpeckRunner(SpeckInfo speck, IRunSupervisor supervisor) {
    this.speck = speck;
    this.supervisor = supervisor;
  }

  public int run() {
    supervisor.beforeSpeck(speck);
    invoke(this, createDoRunInfo());
    supervisor.afterSpeck();

    return resetStatus(SPECK);
  }

  private MethodInfo createDoRunInfo() {
    MethodInfo result = new MethodInfo();
    result.setParent(speck);
    result.setKind(MethodKind.SPECK_EXECUTION);
    result.setReflection(DO_RUN);
    result.setMetadata(speck.getMetadata());
    for (IMethodInterceptor interceptor : speck.getInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  /**
   * Do not call directly.
   */
  public void doRun() {
    createSpeckInstance(true);
    invokeSetupSpeck(speck);
    runFeatures(speck);
    invokeCleanupSpeck(speck);
  }

  protected void createSpeckInstance(boolean shared) {
    if (runStatus != OK) return;

    try {
      if (shared) {
        sharedInstance = speck.getReflection().newInstance();
        // make sure that x.getOrSetSomeSharedField() also works for x == sharedInstance
        // (important for setupSpeck/cleanupSpeck)
        speck.getSharedInstanceField().getReflection().set(sharedInstance, sharedInstance);
      } else {
        currentInstance = speck.getReflection().newInstance();
        speck.getSharedInstanceField().getReflection().set(currentInstance, sharedInstance);
      }
    } catch (Throwable t) {
      throw new InternalSpockError("Failed to instantiate Speck '%s'", t).withArgs(speck.getName());
    }
  }

  private void invokeSetupSpeck(SpeckInfo speck) {
    if (speck == null) return;
    invokeSetupSpeck(speck.getSuperSpeck());
    if (runStatus != OK) return;
    invoke(sharedInstance, speck.getSetupSpeckMethod());
  }

  private void runFeatures(SpeckInfo speck) {
    if (speck == null) return;
    runFeatures(speck.getSuperSpeck());
    if (runStatus != OK) return;

    for (FeatureInfo feature : speck.getFeatures()) {
      runFeature(feature);
      if (resetStatus(FEATURE) != OK) return;
    }
  }

  private void invokeCleanupSpeck(SpeckInfo speck) {
    if (speck == null) return;
    invoke(sharedInstance, speck.getCleanupSpeckMethod());
    if (action(runStatus) == ABORT) return;
    invokeCleanupSpeck(speck.getSuperSpeck());
  }

  private void runFeature(FeatureInfo feature) {
    if (runStatus != OK) return;

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
   * Do not call directly.
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

    createSpeckInstance(false);
    invokeSetup(speck);
    invokeFeatureMethod(feature.getFeatureMethod());
    invokeCleanup(speck);
  }

  protected int resetStatus(int scope) {
    if (scope(runStatus) <= scope) runStatus = OK;
    return runStatus;
  }

  protected void runParameterizedFeature(FeatureInfo feature) {
    throw new UnsupportedOperationException("This runner cannot run parameterized features");
  }

  protected void invokeSetup(SpeckInfo speck) {
    if (speck == null) return;
    invokeSetup(speck.getSuperSpeck());
    if (runStatus != OK) return;
    invoke(speck.getSetupMethod());
  }

  protected void invokeFeatureMethod(MethodInfo feature, Object... args) {
    if (runStatus != OK) return;
    invoke(feature, args);
  }

  protected void invokeCleanup(SpeckInfo speck) {
    if (speck == null) return;
    invoke(speck.getCleanupMethod());
    if (action(runStatus) == ABORT) return;
    invokeCleanup(speck.getSuperSpeck());
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

