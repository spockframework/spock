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
public class SpeckInfoBaseRunner {
  private static final Method DO_RUN;
  private static final Method DO_RUN_FEATURE;

  private final SpeckInfo speck;
  // This runner uses a single instance of the user-provided class for running
  // the whole Speck. It relies on the compiler to ensure that shared and
  // non-shared fields are initialized in setupSpeck() and setup(),
  // respectively.
  private Object instance;
  protected final IRunSupervisor supervisor;
  protected int runStatus = OK;

  static {
    try {
      DO_RUN = SpeckInfoBaseRunner.class.getMethod("doRun");
      DO_RUN_FEATURE = SpeckInfoBaseRunner.class.getMethod("doRunFeature", FeatureInfo.class);
    } catch (NoSuchMethodException e) {
      throw new InternalSpockError();
    }
  }

  public SpeckInfoBaseRunner(SpeckInfo speck, IRunSupervisor supervisor) {
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
    createSpeckInstance();
    invokeSetupSpeck();
    runFeatures();
    invokeCleanupSpeck();
  }

  private void createSpeckInstance() {
    if (runStatus != OK) return;
    
    try {
      instance = speck.getReflection().newInstance();
    } catch (Throwable t) {
      throw new InternalSpockError("Failed to instantiate Speck '%s'", t).withArgs(speck.getName());
    }
  }

  private void invokeSetupSpeck() {
    if (runStatus != OK) return;
    invoke(speck.getSetupSpeckMethod());
  }

  private void runFeatures() {
    if (runStatus != OK) return;

    for (FeatureInfo feature : speck.getFeatures()) {
      runFeature(feature);
      if (resetStatus(FEATURE) != OK) return;
    }
  }

  private void invokeCleanupSpeck() {
    if (action(runStatus) == ABORT) return;
    invoke(speck.getCleanupSpeckMethod());
  }

  private void runFeature(FeatureInfo feature) {
    if (runStatus != OK) return;

    supervisor.beforeFeature(feature);
    invoke(this, createDoRunFeatureInfo(feature), feature);
    supervisor.afterFeature();
  }

  private MethodInfo createDoRunFeatureInfo(FeatureInfo feature) {
    MethodInfo result = new MethodInfo();
    result.setParent(speck);
    result.setKind(MethodKind.FEATURE_EXECUTION);
    result.setReflection(DO_RUN_FEATURE);
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

    invokeSetup();
    invokeFeatureMethod(feature.getFeatureMethod());
    invokeCleanup();
  }

  protected int resetStatus(int scope) {
    if (scope(runStatus) <= scope) runStatus = OK;
    return runStatus;
  }

  protected void runParameterizedFeature(FeatureInfo feature) {
    throw new UnsupportedOperationException("This runner cannot run parameterized features");
  }

  protected void invokeSetup() {
    if (runStatus != OK) return;
    invoke(speck.getSetupMethod());
  }

  protected void invokeFeatureMethod(MethodInfo feature, Object... args) {
    if (runStatus != OK) return;
    invoke(feature, args);
  }

  protected void invokeCleanup() {
    if (action(runStatus) == ABORT) return;
    invoke(speck.getCleanupMethod());
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
    invoke(instance, method, arguments);
  }

  protected Object invokeRaw(Object target, MethodInfo method, Object... arguments) {
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
    return invokeRaw(instance, method, arguments);
  }
}

