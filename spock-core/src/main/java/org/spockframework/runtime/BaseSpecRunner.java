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

import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.MethodInvocation;
import org.spockframework.runtime.model.*;
import org.spockframework.util.CollectionUtil;
import org.spockframework.util.InternalSpockError;

import spock.lang.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.spockframework.runtime.RunStatus.*;

/**
 * Executes a single Spec. Notifies its supervisor about overall execution
 * progress and every invocation of Spec code.
 * Supervisor also determines the error strategy.
 *
 * @author Peter Niederwieser
 */
public class BaseSpecRunner {
  protected static final Object[] EMPTY_ARGS = new Object[0];

  protected final SpecInfo spec;
  protected final IRunSupervisor supervisor;

  protected FeatureInfo currentFeature;
  protected IterationInfo currentIteration;

  protected Specification sharedInstance;
  protected Specification currentInstance;
  protected int runStatus = OK;

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
    runSharedInitializer();
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
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) {
        doRunSpec();
        return null;
      }
    };
    result.setParent(spec);
    result.setKind(MethodKind.SPEC_EXECUTION);
    result.setDescription(spec.getDescription());
    for (IMethodInterceptor interceptor : spec.getInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  public void doRunSpec() {
    runSetupSpec();
    runFeatures();
    runCleanupSpec();
  }

  private void createSpecInstance(boolean shared) {
    if (runStatus != OK) return;

    try {
      if (shared) {
        sharedInstance = (Specification) spec.getReflection().newInstance();
        currentInstance = sharedInstance;
      } else {
        currentInstance = (Specification) spec.getReflection().newInstance();
      }
    } catch (Throwable t) {
      throw new InternalSpockError("Failed to instantiate spec '%s'", t).withArgs(spec.getName());
    }

    getSpecificationContext().setCurrentSpec(spec);
    getSpecificationContext().setSharedInstance(sharedInstance);
  }

  private void runSharedInitializer() {
    runSharedInitializer(spec);
  }

  private void runSharedInitializer(SpecInfo spec) {
    if (spec == null) return;
    invoke(this, createMethodInfoForDoRunSharedInitializer(spec), spec);
  }

  private MethodInfo createMethodInfoForDoRunSharedInitializer(final SpecInfo spec) {
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) {
        doRunSharedInitializer(spec);
        return null;
      }
    };
    result.setParent(spec);
    result.setKind(MethodKind.SHARED_INITIALIZER);
    result.setDescription(spec.getDescription());
    for (IMethodInterceptor interceptor : spec.getSharedInitializerInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  public void doRunSharedInitializer(SpecInfo spec) {
    runSharedInitializer(spec.getSuperSpec());
    if (runStatus != OK) return;
    invoke(currentInstance, spec.getSharedInitializerMethod());
  }

  private void runSetupSpec() {
    runSetupSpec(spec);
  }

  private void runSetupSpec(SpecInfo spec) {
    if (spec == null) return;
    invoke(this, createMethodInfoForDoRunSetupSpec(spec), spec);
  }

  private MethodInfo createMethodInfoForDoRunSetupSpec(final SpecInfo spec) {
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) {
        doRunSetupSpec(spec);
        return null;
      }
    };
    result.setParent(spec);
    result.setKind(MethodKind.SETUP_SPEC);
    result.setDescription(spec.getDescription());
    for (IMethodInterceptor interceptor : spec.getSetupSpecInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  public void doRunSetupSpec(SpecInfo spec) {
    runSetupSpec(spec.getSuperSpec());
    for (MethodInfo method : spec.getSetupSpecMethods()) {
      if (runStatus != OK) return;
      invoke(currentInstance, method);
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

  private void runCleanupSpec() {
    currentInstance = sharedInstance;
    runCleanupSpec(spec);
  }

  private void runCleanupSpec(SpecInfo spec) {
    if (spec == null) return;
    invoke(this, createMethodForDoRunCleanupSpec(spec), spec);
  }

  private MethodInfo createMethodForDoRunCleanupSpec(final SpecInfo spec) {
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) {
        doRunCleanupSpec(spec);
        return null;
      }
    };
    result.setParent(spec);
    result.setKind(MethodKind.CLEANUP_SPEC);
    result.setDescription(spec.getDescription());
    for (IMethodInterceptor interceptor : spec.getCleanupSpecInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  public void doRunCleanupSpec(SpecInfo spec) {
    for (MethodInfo method : spec.getCleanupSpecMethods()) {
      if (action(runStatus) == ABORT) return;
      invoke(currentInstance, method);
    }
    runCleanupSpec(spec.getSuperSpec());
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
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) {
        doRunFeature();
        return null;
      }
    };
    result.setParent(currentFeature.getParent());
    result.setKind(MethodKind.FEATURE_EXECUTION);
    result.setFeature(currentFeature);
    result.setDescription(currentFeature.getDescription());
    for (IMethodInterceptor interceptor : currentFeature.getInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  public void doRunFeature() {
    currentFeature.setIterationNameProvider(new SafeIterationNameProvider(currentFeature.getIterationNameProvider()));
    if (currentFeature.isParameterized())
      runParameterizedFeature();
    else runSimpleFeature();
  }

  private void runSimpleFeature() {
    if (runStatus != OK) return;

    initializeAndRunIteration(Collections.<String, Object>emptyMap(), 1);
    resetStatus(ITERATION);
  }

  protected void initializeAndRunIteration(Map<String, Object> dataValues, int estimatedNumIterations) {
    if (runStatus != OK) return;

    createSpecInstance(false);
    runInitializer();
    runIteration(dataValues, estimatedNumIterations);
  }

  private void runIteration(Map<String, Object> dataValues, int estimatedNumIterations) {
    if (runStatus != OK) return;

    currentIteration = createIterationInfo(dataValues, estimatedNumIterations);
    getSpecificationContext().setCurrentIteration(currentIteration);

    supervisor.beforeIteration(currentIteration);
    invoke(this, createMethodInfoForDoRunIteration());
    supervisor.afterIteration(currentIteration);

    getSpecificationContext().setCurrentIteration(null);
    currentIteration = null;
  }

  private IterationInfo createIterationInfo(Map<String, Object> dataValues, int estimatedNumIterations) {
    IterationInfo result = new IterationInfo(currentFeature, dataValues, estimatedNumIterations);
    String iterationName = currentFeature.getIterationNameProvider().getName(result);
    result.setName(iterationName);
    Description description = Description.createTestDescription(spec.getReflection(),
        iterationName, currentFeature.getFeatureMethod().getAnnotations());
    result.setDescription(description);
    return result;
  }

  private MethodInfo createMethodInfoForDoRunIteration() {
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) {
        doRunIteration();
        return null;
      }
    };
    result.setParent(currentFeature.getParent());
    result.setKind(MethodKind.ITERATION_EXECUTION);
    result.setFeature(currentFeature);
    result.setDescription(currentFeature.getDescription());
    for (IMethodInterceptor interceptor : currentFeature.getIterationInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  public void doRunIteration() {
    runSetup();
    runFeatureMethod();
    runCleanup();
  }

  protected int resetStatus(int scope) {
    if (scope(runStatus) <= scope) runStatus = OK;
    return runStatus;
  }

  protected void runParameterizedFeature() {
    throw new UnsupportedOperationException("This runner cannot run parameterized features");
  }

  private void runInitializer() {
    runInitializer(spec);
  }

  private void runInitializer(SpecInfo spec) {
    if (spec == null) return;
    invoke(this, createMethodInfoForDoRunInitializer(spec), spec);
  }

  private MethodInfo createMethodInfoForDoRunInitializer(final SpecInfo spec) {
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) {
        doRunInitializer(spec);
        return null;
      }
    };
    result.setParent(currentFeature.getParent());
    result.setKind(MethodKind.INITIALIZER);
    result.setFeature(currentFeature);
    result.setDescription(currentFeature.getDescription());
    for (IMethodInterceptor interceptor : spec.getInitializerInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  public void doRunInitializer(SpecInfo spec) {
    runInitializer(spec.getSuperSpec());
    if (runStatus != OK) return;
    invoke(currentInstance, spec.getInitializerMethod());
  }

  private void runSetup() {
    runSetup(spec);
  }

  private void runSetup(SpecInfo spec) {
    if (spec == null) return;
    invoke(this, createMethodInfoForDoRunSetup(spec), spec);
  }

  private MethodInfo createMethodInfoForDoRunSetup(final SpecInfo spec) {
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) {
        doRunSetup(spec);
        return null;
      }
    };
    result.setParent(currentFeature.getParent());
    result.setKind(MethodKind.SETUP);
    result.setFeature(currentFeature);
    result.setDescription(currentFeature.getDescription());
    for (IMethodInterceptor interceptor : spec.getSetupInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  public void doRunSetup(SpecInfo spec) {
    runSetup(spec.getSuperSpec());
    for (MethodInfo method : spec.getSetupMethods()) {
      if (runStatus != OK) return;
      method.setFeature(currentFeature);
      invoke(currentInstance, method);
    }
  }

  private void runFeatureMethod() {
    if (runStatus != OK) return;
    invoke(currentInstance, currentFeature.getFeatureMethod(), dataValuesToArray(currentFeature.getParameterNames(), currentIteration.getDataValues()));
  }

  private Object[] dataValuesToArray(List<String> parameterNames, Map<String, Object> dataValues) {
    Object[] result = new Object[parameterNames.size()];
    for (int i = 0; i < parameterNames.size(); i++) {
      final String parameterName = parameterNames.get(i);
      if (dataValues.containsKey(parameterName)){
        result[i] = dataValues.get(parameterName);
      }else {
        throw new SpockExecutionException("Parameter '%s' was not set").withArgs(parameterName);
      }
    }
    return result;
  }

  private void runCleanup() {
    runCleanup(spec);
  }

  private void runCleanup(SpecInfo spec) {
    if (spec == null) return;
    invoke(this, createMethodInfoForDoRunCleanup(spec), spec);
  }

  private MethodInfo createMethodInfoForDoRunCleanup(final SpecInfo spec) {
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) {
        doRunCleanup(spec);
        return null;
      }
    };
    result.setParent(currentFeature.getParent());
    result.setKind(MethodKind.CLEANUP);
    result.setFeature(currentFeature);
    result.setDescription(currentFeature.getDescription());
    for (IMethodInterceptor interceptor : spec.getCleanupInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  public void doRunCleanup(SpecInfo spec) {
    if (spec.getIsBottomSpec()) {
      runIterationCleanups();
      if (action(runStatus) == ABORT) return;
    }
    for (MethodInfo method : spec.getCleanupMethods()) {
      if (action(runStatus) == ABORT) return;
      invoke(currentInstance, method);
    }
    runCleanup(spec.getSuperSpec());
  }

  private void runIterationCleanups() {
    for (Runnable cleanup : currentIteration.getCleanups()) {
      if (action(runStatus) == ABORT) return;
      try {
        cleanup.run();
      } catch (Throwable t) {
        ErrorInfo error = new ErrorInfo(CollectionUtil.getFirstElement(spec.getCleanupMethods()), t);
        runStatus = supervisor.error(error);
      }
    }
  }

  private void invoke(Object target, MethodInfo method, Object... arguments) {
    if (method == null || method.isExcluded()) return;

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
    try {
      return method.invoke(target, arguments);
    } catch (Throwable t) {
      runStatus = supervisor.error(new ErrorInfo(method, t));
      return null;
    }
  }

  protected SpecificationContext getSpecificationContext() {
    return (SpecificationContext) currentInstance.getSpecificationContext();
  }
}

