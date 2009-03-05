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

import static org.spockframework.runtime.RunStatus.*;
import org.spockframework.runtime.intercept.IMethodInterceptor;
import org.spockframework.runtime.intercept.MethodInvocation;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.SpeckInfo;
import org.spockframework.util.InternalSpockError;

/**
 * Executes a single Speck. Notifies its supervisor about overall execution
 * progress and every invocation of Speck code.
 * Supervisor also determines the error strategy.
 *
 * @author Peter Niederwieser
 */

// IDEA: give Speck code access to SpeckInfo object model; would enable API-based extensions
// IDEA: add ability to run iterations w/o recreating fixture in between (setup/cleanup)
// NOTE: Intellij IDEA JUnit runner makes several assumptions about JUnit test classes that
// become problematic once alternative test runners are involved:
// - if a test method has parameters, IDEA can no longer navigate to it from the JUnit pane
// - if the name of a test method contains parentheses (e.g. "foo (bar) foo",
//   IDEA doesn't display the method in the JUnit pane; instead it displays a test suite with zero cases
// - to jump to the line in a test where an assertion failure occurred, IDEA needs
//   to parse the stack trace (we should do better)

// TODO: check exception handling policy (i.e. exceptions thrown and caught)
public class SpeckInfoBaseRunner {
  private final SpeckInfo speck;
  // This runner uses a single instance of the user-provided class for running
  // the whole Speck. It relies on the compiler to ensure that shared and
  // non-shared fields are initialized in setupSpeck() and setup(),
  // respectively.
  private Object instance;
  protected final IRunSupervisor supervisor;
  protected int runStatus = OK;

  private final IMethodInterceptor invoker = new IMethodInterceptor() {
    public void invoke(MethodInvocation invocation) throws Throwable {
      MethodInfo method = invocation.getMethod();
      if (method.isStub()) return;

      try {
        method.getReflection().invoke(invocation.getTarget(), invocation.getArguments());
      } catch (InvocationTargetException e) {
        throw e.getCause(); 
      }
    }
  };

  public SpeckInfoBaseRunner(SpeckInfo speck, IRunSupervisor supervisor) {
    this.speck = speck;
    this.supervisor = supervisor;
  }

  public int run() {
    supervisor.beforeSpeck(speck);
    createSpeckInstance();
    invokeSetupSpeck();
    runFeatures();
    invokeCleanupSpeck();
    supervisor.afterSpeck();
    resetStatus(SPECK);
    return runStatus;
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
    invoke(speck.getSetupSpeckMethod(), null);
  }

  private void runFeatures() {
    if (runStatus != OK) return;

    for (MethodInfo feature : speck.getFeatureMethods()) {
      runFeature(feature);
      if (runStatus != OK) return;
    }
  }

  private void invokeCleanupSpeck() {
    if (action(runStatus) == ABORT) return;
    invoke(speck.getCleanupSpeckMethod(), null);
  }

  private void runFeature(MethodInfo feature) {
    if (runStatus != OK) return;

    supervisor.beforeFeature(feature);
    if (feature.isParameterized())
      runParameterizedFeature(feature);
    else runSimpleFeature(feature);
    supervisor.afterFeature();
    
    resetStatus(FEATURE);
  }

  private void runSimpleFeature(MethodInfo feature) {
    if (runStatus != OK) return;

    invokeSetup();
    invokeFeature(feature, null);
    invokeCleanup();
  }

  protected void resetStatus(int scope) {
    if (scope(runStatus) <= scope) runStatus = OK;
  }

  protected void runParameterizedFeature(MethodInfo feature) {
    throw new UnsupportedOperationException("This runner cannot run parameterized features");
  }

  protected void invokeSetup() {
    if (runStatus != OK) return;
    invoke(speck.getSetupMethod(), null);
  }

  protected void invokeFeature(MethodInfo feature, Object[] args) {
    if (runStatus != OK) return;
    invoke(feature, args);
  }

  protected void invokeCleanup() {
    if (action(runStatus) == ABORT) return;
    invoke(speck.getCleanupMethod(), null);
  }

  protected void invoke(MethodInfo method, Object[] arguments) {
    // fast lane
    if (method.getInterceptors().isEmpty()) {
      invokeRaw(method, arguments);
      return;
    }

    // slow lane
    MethodInvocation invocation = new MethodInvocation(instance, method, arguments,
      method.getInterceptors().iterator(), invoker);
    try {
      invocation.proceed();
    } catch (Throwable t) {
      runStatus = supervisor.error(method, t, runStatus);
    }
  }

  protected Object invokeRaw(MethodInfo method, Object[] arguments) {
    if (method.isStub()) return null;

    try {
      return method.getReflection().invoke(instance, arguments);
    } catch (InvocationTargetException e) {
      runStatus = supervisor.error(method, e.getTargetException(), runStatus);
      return null;
    } catch (Throwable t) { // TODO: why doesn't invoker do the same? why not throw InternalSpockError (if at all)?
      throw new InternalSpockError("Failed to invoke method '%s'", t).withArgs(method.getReflection().getName());
    }
  }
}

