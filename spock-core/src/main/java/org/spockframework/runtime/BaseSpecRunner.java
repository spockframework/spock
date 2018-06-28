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

import org.spockframework.runtime.extension.*;
import org.spockframework.runtime.model.*;
import org.spockframework.util.*;
import spock.lang.Specification;

import java.util.List;
import org.junit.runner.Description;

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
  protected static final FeatureInfo NO_CURRENT_FEATURE = null;
  protected static final IterationInfo NO_CURRENT_ITERATION = null;

  protected final SpecInfo spec;
  protected final IRunSupervisor supervisor;
  protected final Scheduler scheduler;

  protected Specification sharedInstance;
  protected ThreadLocal<Integer> runStatus = new ThreadLocal<Integer>(){
    @Override
    protected Integer initialValue() {
      return OK;
    }
  };

  public BaseSpecRunner(SpecInfo spec, IRunSupervisor supervisor, Scheduler scheduler) {
    this.spec = spec;
    this.supervisor = supervisor;
    this.scheduler = scheduler;
  }

  public int run() {
    runStatus.set(OK);
    // Sometimes a spec run is requested even though the spec has been excluded
    // (e.g. if JUnit is in control). In such a case, the best thing we can do
    // is to treat the spec as skipped.
    if (spec.isExcluded() || spec.isSkipped()) {
      supervisor.specSkipped(spec);
      return OK;
    }

    try {
        sharedInstance = (Specification) spec.getReflection().newInstance();
    } catch (Throwable t) {
      throw new InternalSpockError("Failed to instantiate spec '%s'", t).withArgs(spec.getName());
    }

    sharedInstance.getSpecificationContext().setCurrentSpec(spec);
    sharedInstance.getSpecificationContext().setSharedInstance(sharedInstance);

    runSharedInitializer();
    runSpec();

    return resetStatus(SPEC);
  }

  private void runSpec() {
    if (runStatus.get() != OK) return;

    supervisor.beforeSpec(spec);
    invoke(NO_CURRENT_FEATURE, NO_CURRENT_ITERATION, sharedInstance, this, createMethodInfoForDoRunSpec());
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

  private void runSharedInitializer() {
    runSharedInitializer(spec);
  }

  private void runSharedInitializer(SpecInfo spec) {
    if (spec == null) return;
    invoke(NO_CURRENT_FEATURE, NO_CURRENT_ITERATION, sharedInstance, this, createMethodInfoForDoRunSharedInitializer(spec), spec);
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
    if (runStatus.get() != OK) return;
    invoke(NO_CURRENT_FEATURE, NO_CURRENT_ITERATION, sharedInstance, sharedInstance, spec.getSharedInitializerMethod());
  }

  private void runSetupSpec() {
    runSetupSpec(spec);
  }

  private void runSetupSpec(SpecInfo spec) {
    if (spec == null) return;
    invoke(NO_CURRENT_FEATURE, NO_CURRENT_ITERATION, sharedInstance, this, createMethodInfoForDoRunSetupSpec(spec), spec);
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
      if (runStatus.get() != OK) return;
      invoke(NO_CURRENT_FEATURE, NO_CURRENT_ITERATION, sharedInstance, sharedInstance, method);
    }
  }

  private void runFeatures() {
    List<FeatureInfo> allFeaturesInExecutionOrder = spec.getAllFeaturesInExecutionOrder();
    Scheduler scheduler = this.scheduler.deriveScheduler(!spec.isSupportParallelExecution());
    final int featureCount = allFeaturesInExecutionOrder.size();
    for (int i = 0; i < featureCount; i++) {
      final FeatureInfo feature = allFeaturesInExecutionOrder.get(i);
      if (resetStatus(FEATURE) != OK) return;

      final boolean isLastFeature = i == featureCount - 1;
      if (isLastFeature) {// let's not waste threads and execute last iteration in current
        runFeature(feature);
      } else {
        scheduler.schedule(new Runnable() {
          @Override
          public void run() {
            runStatus.set(OK);
            runFeature(feature);
          }
        });
      }
    }
    scheduler.waitFinished();
  }

  private void runCleanupSpec() {
    runCleanupSpec(spec);
  }

  private void runCleanupSpec(SpecInfo spec) {
    if (spec == null) return;
    invoke(NO_CURRENT_FEATURE, NO_CURRENT_ITERATION, sharedInstance, this, createMethodForDoRunCleanupSpec(spec), spec);
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
      if (action(runStatus.get()) == ABORT) return;
      invoke(NO_CURRENT_FEATURE, NO_CURRENT_ITERATION, sharedInstance, sharedInstance, method);
    }
    runCleanupSpec(spec.getSuperSpec());
  }

  private void runFeature(FeatureInfo feature) {
    if (runStatus.get() != OK) return;

    if (feature.isExcluded()) return;

    if (feature.isSkipped()) {
      supervisor.featureSkipped(feature);
      return;
    }

    supervisor.beforeFeature(feature);
    invoke(feature, NO_CURRENT_ITERATION, sharedInstance, this, createMethodInfoForDoRunFeature(feature));
    supervisor.afterFeature(feature);
  }

  private MethodInfo createMethodInfoForDoRunFeature(final FeatureInfo feature) {
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) {
        doRunFeature(feature);
        return null;
      }
    };
    result.setParent(feature.getParent());
    result.setKind(MethodKind.FEATURE_EXECUTION);
    result.setFeature(feature);
    result.setDescription(feature.getDescription());
    for (IMethodInterceptor interceptor : feature.getInterceptors()){
      result.addInterceptor(interceptor);
    }
    return result;
  }

  public void doRunFeature(FeatureInfo feature) {
    feature.setIterationNameProvider(new SafeIterationNameProvider(feature.getIterationNameProvider()));
    if (feature.isParameterized()){
      runParameterizedFeature(feature);
    }else {
      runSimpleFeature(feature);
    }
  }

  private void runSimpleFeature(FeatureInfo feature) {
    if (runStatus.get() != OK) return;

    initializeAndRunIteration(feature, EMPTY_ARGS, 1);
    resetStatus(ITERATION);
  }

  protected void initializeAndRunIteration(FeatureInfo feature, Object[] dataValues, int estimatedNumIterations) {
    if (runStatus.get() != OK) return;

    Specification currentInstance;
    try {
      currentInstance = (Specification) spec.getReflection().newInstance();
    } catch (Throwable t) {
      throw new InternalSpockError("Failed to instantiate spec '%s'", t).withArgs(spec.getName());
    }

    currentInstance.getSpecificationContext().setCurrentSpec(spec);
    currentInstance.getSpecificationContext().setSharedInstance(sharedInstance);

    runInitializer(feature, currentInstance);
    runIteration(feature, currentInstance, dataValues, estimatedNumIterations);
  }

  private void runIteration(FeatureInfo feature, Specification currentInstance, Object[] dataValues, int estimatedNumIterations) {
    if (runStatus.get() != OK) return;

    IterationInfo currentIteration = createIterationInfo(feature, dataValues, estimatedNumIterations);
    currentInstance.getSpecificationContext().setCurrentIteration(currentIteration);

    supervisor.beforeIteration(feature, currentIteration);
    invoke(feature, currentIteration, currentInstance, this, createMethodInfoForDoRunIteration(feature, currentInstance, currentIteration));
    supervisor.afterIteration(feature, currentIteration);

    currentInstance.getSpecificationContext().setCurrentIteration(null);
  }

  private IterationInfo createIterationInfo(FeatureInfo feature, Object[] dataValues, int estimatedNumIterations) {
    IterationInfo result = new IterationInfo(feature, dataValues, estimatedNumIterations);
    String iterationName = feature.getIterationNameProvider().getName(result);
    result.setName(iterationName);
    Description description = Description.createTestDescription(spec.getReflection(),
        iterationName, feature.getFeatureMethod().getAnnotations());
    result.setDescription(description);
    return result;
  }

  private MethodInfo createMethodInfoForDoRunIteration(final FeatureInfo feature, final Specification currentInstance, final IterationInfo currentIteration) {
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) {
        doRunIteration(feature, currentInstance, currentIteration);
        return null;
      }
    };
    result.setParent(feature.getParent());
    result.setKind(MethodKind.ITERATION_EXECUTION);
    result.setFeature(feature);
    result.setDescription(feature.getDescription());
    result.setIteration(currentIteration);
    for (IMethodInterceptor interceptor : feature.getIterationInterceptors()){
      result.addInterceptor(interceptor);
    }
    return result;
  }

  public void doRunIteration(FeatureInfo feature, Specification currentInstance, IterationInfo currentIteration) {
    runSetup(feature, currentInstance, currentIteration);
    runFeatureMethod(feature, currentInstance, currentIteration);
    runCleanup(feature, currentInstance, currentIteration);
  }

  protected int resetStatus(int scope) {
    if (scope(runStatus.get()) <= scope) runStatus.set(OK);
    return runStatus.get();
  }

  protected void runParameterizedFeature(FeatureInfo currentFeature) {
    throw new UnsupportedOperationException("This runner cannot run parameterized features");
  }

  private void runInitializer(FeatureInfo feature, final Specification currentInstance) {
    runInitializer(spec, feature, currentInstance);
  }

  private void runInitializer(SpecInfo spec, FeatureInfo feature, final Specification currentInstance) {
    if (spec == null) return;
    invoke(feature, NO_CURRENT_ITERATION, currentInstance, this, createMethodInfoForDoRunInitializer(spec, feature, currentInstance), spec);
  }

  private MethodInfo createMethodInfoForDoRunInitializer(final SpecInfo spec, final FeatureInfo feature, final Specification currentInstance) {
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) {
        doRunInitializer(spec, feature, currentInstance);
        return null;
      }
    };
    result.setParent(feature.getParent());
    result.setKind(MethodKind.INITIALIZER);
    result.setFeature(feature);
    result.setDescription(feature.getDescription());
    for (IMethodInterceptor interceptor : spec.getInitializerInterceptors()){
      result.addInterceptor(interceptor);
    }
    return result;
  }

  public void doRunInitializer(SpecInfo spec, FeatureInfo feature, Specification currentInstance) {
    runInitializer(spec.getSuperSpec(), feature, currentInstance);
    if (runStatus.get() != OK) return;
    invoke(feature, NO_CURRENT_ITERATION, currentInstance, currentInstance, spec.getInitializerMethod());
  }

  private void runSetup(FeatureInfo feature, Specification currentInstance, IterationInfo currentIteration) {
    runSetup(spec, feature, currentInstance, currentIteration);
  }

  private void runSetup(SpecInfo spec, FeatureInfo feature, Specification currentInstance, IterationInfo currentIteration) {
    if (spec == null) return;
    invoke(feature, currentIteration, currentInstance, this, createMethodInfoForDoRunSetup(spec, feature, currentInstance, currentIteration), spec);
  }

  private MethodInfo createMethodInfoForDoRunSetup(final SpecInfo spec, final FeatureInfo feature, final Specification currentInstance, final IterationInfo currentIteration) {
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) {
        doRunSetup(spec, feature, currentInstance, currentIteration);
        return null;
      }
    };
    result.setParent(feature.getParent());
    result.setKind(MethodKind.SETUP);
    result.setFeature(feature);
    result.setDescription(feature.getDescription());
    result.setIteration(currentIteration);
    for (IMethodInterceptor interceptor : spec.getSetupInterceptors()){
      result.addInterceptor(interceptor);
    }
    return result;
  }

  public void doRunSetup(SpecInfo spec, FeatureInfo feature, final Specification currentInstance, IterationInfo currentIteration) {
    runSetup(spec.getSuperSpec(), feature, currentInstance, currentIteration);
    for (MethodInfo method : spec.getSetupMethods()) {
      if (runStatus.get() != OK){
        return;
      }
      invoke(feature, currentIteration, currentInstance, currentInstance, method);
    }
  }

  private void runFeatureMethod(FeatureInfo feature, final Specification currentInstance, IterationInfo currentIteration) {
    if (runStatus.get() != OK){
      return;
    }
    invoke(feature, currentIteration, currentInstance, currentInstance, feature.getFeatureMethod(), currentIteration.getDataValues());
  }

  private void runCleanup(FeatureInfo feature, final Specification currentInstance, IterationInfo currentIteration) {
    runCleanup(spec, feature, currentInstance, currentIteration);
  }

  private void runCleanup(SpecInfo spec, FeatureInfo feature, final Specification currentInstance, IterationInfo currentIteration) {
    if (spec == null) return;
    invoke(feature, currentIteration, currentInstance, this, createMethodInfoForDoRunCleanup(spec, feature, currentInstance, currentIteration), spec);
  }

  private MethodInfo createMethodInfoForDoRunCleanup(final SpecInfo spec, final FeatureInfo feature, final Specification currentInstance, final IterationInfo currentIteration) {
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) {
        doRunCleanup(spec, feature, currentInstance, currentIteration);
        return null;
      }
    };
    result.setParent(feature.getParent());
    result.setKind(MethodKind.CLEANUP);
    result.setFeature(feature);
    result.setDescription(feature.getDescription());
    result.setIteration(currentIteration);
    for (IMethodInterceptor interceptor : spec.getCleanupInterceptors()){
      result.addInterceptor(interceptor);
    }
    return result;
  }

  public void doRunCleanup(SpecInfo spec, FeatureInfo feature, final Specification currentInstance, IterationInfo currentIteration) {
    if (spec.getIsBottomSpec()) {
      runIterationCleanups(feature, currentIteration);
      if (action(runStatus.get()) == ABORT) return;
    }
    for (MethodInfo method : spec.getCleanupMethods()) {
      if (action(runStatus.get()) == ABORT) return;
      invoke(feature, currentIteration, currentInstance, currentInstance, method);
    }
    runCleanup(spec.getSuperSpec(), feature, currentInstance, currentIteration);
  }

  private void runIterationCleanups(FeatureInfo currentFeature, IterationInfo currentIteration) {
    for (Runnable cleanup : currentIteration.getCleanups()) {
      if (action(runStatus.get()) == ABORT) return;
      try {
        cleanup.run();
      } catch (Throwable t) {
        ErrorInfo error = new ErrorInfo(CollectionUtil.getFirstElement(spec.getCleanupMethods()), t);
        runStatus.set(supervisor.error(currentFeature, currentIteration, error));
      }
    }
  }

  private void invoke(FeatureInfo currentFeature, IterationInfo currentIteration, final Specification currentInstance, Object target, MethodInfo method, Object... arguments) {
    if (method == null || method.isExcluded()) return;

    // fast lane
    if (method.getInterceptors().isEmpty()) {
      invokeRaw(currentFeature, currentIteration, target, method, arguments);
      return;
    }

    // slow lane
    MethodInvocation invocation = new MethodInvocation(currentFeature, currentIteration, sharedInstance, currentInstance, target, method, arguments);
    try {
      invocation.proceed();
    } catch (Throwable t) {
      ErrorInfo error = new ErrorInfo(method, t);
      runStatus.set(supervisor.error(currentFeature, currentIteration, error));
    }
  }

  protected Object invokeRaw(FeatureInfo currentFeature, IterationInfo currentIteration, Object target, MethodInfo method, Object... arguments) {
    try {
      return method.invoke(target, arguments);
    } catch (Throwable t) {
      runStatus.set(supervisor.error(currentFeature, currentIteration, new ErrorInfo(method, t)));
      return null;
    }
  }
}

