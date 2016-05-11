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

import java.util.ArrayList;
import java.util.List;

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

  public BaseSpecRunner(SpecInfo spec, IRunSupervisor supervisor, Scheduler scheduler) {
    this.spec = spec;
    this.supervisor = supervisor;
    this.scheduler = scheduler;
  }

  public void run() {
    // Sometimes a spec run is requested even though the spec has been excluded
    // (e.g. if JUnit is in control). In such a case, the best thing we can do
    // is to treat the spec as skipped.
    if (spec.isExcluded() || spec.isSkipped()) {
      supervisor.specSkipped(spec);
      return;
    }

    sharedInstance = createNewInstance(true);

    try {
      runSharedInitializer();
      runSpec();
    } catch (InvokeException ie) {
      supervisor.error(ie);
    } catch (MultipleInvokeException me){
      supervisor.error(me);
    }
  }

  private Specification createNewInstance(boolean shared) {
    Specification instance;
    try {
      instance = (Specification) spec.getReflection().newInstance();
    } catch (Throwable t) {
      throw new InternalSpockError("Failed to instantiate spec '%s'", t).withArgs(spec.getName());
    }

    instance.getSpecificationContext().setCurrentSpec(spec);
    if (shared){
      instance.getSpecificationContext().setSharedInstance(instance);
    }else {
      instance.getSpecificationContext().setSharedInstance(sharedInstance);
    }
    return instance;
  }

  private void runSpec() throws InvokeException, MultipleInvokeException {
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
    try {
      runSetupSpec();
      runFeatures();
    } catch (InvokeException ie) {
      supervisor.error(ie);
    } catch (MultipleInvokeException e) {
      supervisor.error(e);
    }
    runCleanupSpec();
  }

  private void runSharedInitializer() throws InvokeException, MultipleInvokeException {
    runSharedInitializer(spec);
  }

  private void runSharedInitializer(SpecInfo spec) throws InvokeException, MultipleInvokeException {
    invoke(NO_CURRENT_FEATURE, NO_CURRENT_ITERATION, sharedInstance, this, createMethodInfoForDoRunSharedInitializer(spec), spec);
  }

  private MethodInfo createMethodInfoForDoRunSharedInitializer(final SpecInfo spec) {
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) throws InvokeException, MultipleInvokeException {
        final SpecInfo superSpec = spec.getSuperSpec();
        if (superSpec != null) {
          runSharedInitializer(superSpec);
        }
        BaseSpecRunner.this.invoke(NO_CURRENT_FEATURE, NO_CURRENT_ITERATION, sharedInstance, sharedInstance, spec.getSharedInitializerMethod());
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

  private void runSetupSpec() throws InvokeException, MultipleInvokeException {
    runSetupSpec(spec);
  }

  private void runSetupSpec(SpecInfo spec) throws InvokeException, MultipleInvokeException {
    invoke(NO_CURRENT_FEATURE, NO_CURRENT_ITERATION, sharedInstance, this, createMethodInfoForDoRunSetupSpec(spec), spec);
  }

  private MethodInfo createMethodInfoForDoRunSetupSpec(final SpecInfo spec) throws InvokeException {
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) throws InvokeException, MultipleInvokeException {
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

  public void doRunSetupSpec(SpecInfo spec) throws InvokeException, MultipleInvokeException {
    final SpecInfo superSpec = spec.getSuperSpec();
    if (superSpec != null) {
      runSetupSpec(superSpec);
    }
    for (MethodInfo method : spec.getSetupSpecMethods()) {
      invoke(NO_CURRENT_FEATURE, NO_CURRENT_ITERATION, sharedInstance, sharedInstance, method);
    }
  }

  private void runFeatures() {
    List<FeatureInfo> allFeaturesInExecutionOrder = spec.getAllFeaturesInExecutionOrder();
    Scheduler scheduler = this.scheduler.deriveScheduler(!spec.isSupportParallelExecution());
    final int featureCount = allFeaturesInExecutionOrder.size();
    for (int i = 0; i < featureCount; i++) {
      final FeatureInfo feature = allFeaturesInExecutionOrder.get(i);

      final boolean isLastFeature = i == featureCount - 1;
      if (isLastFeature) {// let's not waste threads and execute last iteration in current
        runFeature(feature);
      } else {
        scheduler.schedule(new Runnable() {
          @Override
          public void run() {
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
    try {
      invoke(NO_CURRENT_FEATURE, NO_CURRENT_ITERATION, sharedInstance, this, createMethodForDoRunCleanupSpec(spec), spec);
    } catch (InvokeException e) {
      supervisor.error(e);
    } catch (MultipleInvokeException e) {
      for (InvokeException invokeException : e.getInvokeExceptions()) {
        supervisor.error(invokeException);
      }
    }
  }

  private MethodInfo createMethodForDoRunCleanupSpec(final SpecInfo spec) {
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) throws InvokeException {
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
      try {
        invoke(NO_CURRENT_FEATURE, NO_CURRENT_ITERATION, sharedInstance, sharedInstance, method);
      } catch (InvokeException e) {
        supervisor.error(e);
      } catch (MultipleInvokeException e) {
        supervisor.error(e);
      }
    }
    final SpecInfo superSpec = spec.getSuperSpec();
    if (superSpec != null) {
      runCleanupSpec(superSpec);
    }
  }

  private void runFeature(FeatureInfo feature) {
    if (feature.isExcluded()) return;

    if (feature.isSkipped()) {
      supervisor.featureSkipped(feature);
      return;
    }

    supervisor.beforeFeature(feature);
    try {
      invoke(feature, NO_CURRENT_ITERATION, sharedInstance, this, createMethodInfoForDoRunFeature(feature));
    } catch (InvokeException e) {
      supervisor.error(e);
    } catch (MultipleInvokeException e) {
      supervisor.error(e);
    }
    supervisor.afterFeature(feature);
  }

  private MethodInfo createMethodInfoForDoRunFeature(final FeatureInfo feature) {
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) throws InvokeException, MultipleInvokeException {
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

  public void doRunFeature(FeatureInfo feature) throws InvokeException, MultipleInvokeException {
    feature.setIterationNameProvider(new SafeIterationNameProvider(feature.getIterationNameProvider()));
    if (feature.isParameterized()){
      runParameterizedFeature(feature);
    }else {
      runSimpleFeature(feature);
    }
  }

  private void runSimpleFeature(FeatureInfo feature) throws InvokeException {
    initializeAndRunIteration(feature, EMPTY_ARGS, 1);
  }

  protected void initializeAndRunIteration(FeatureInfo feature, Object[] dataValues, int estimatedNumIterations) throws InvokeException {
    final int attemptsCount = feature.getRetryCount() + 1;

    IterationInfo currentIteration = createIterationInfo(feature, dataValues, estimatedNumIterations);

    supervisor.beforeIteration(feature, currentIteration);

    for (int attempt = 0; attempt < attemptsCount; attempt++) {
      boolean reportFailures = attempt == attemptsCount - 1; // report only on last attempt
      try {
        IterationInfo currentIterationForAttempt = currentIteration.copy();
        Specification currentInstance = createNewInstance(false);

        runInitializer(feature, currentInstance);
        runIteration(feature, currentInstance, currentIterationForAttempt, reportFailures);

        break;
      } catch (InvokeException ie) {
        if (reportFailures) {
          supervisor.error(ie);
        }
      } catch (MultipleInvokeException me) {
        if (reportFailures) {
          supervisor.error(me);
        }
      }
    }

    supervisor.afterIteration(feature, currentIteration);
  }

  private void runIteration(FeatureInfo feature, Specification currentInstance, IterationInfo currentIteration, boolean reportFailures) throws InvokeException, MultipleInvokeException {
    currentInstance.getSpecificationContext().setCurrentIteration(currentIteration);

    invoke(feature, currentIteration, currentInstance, this, createMethodInfoForDoRunIteration(feature, currentInstance, currentIteration, reportFailures));
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

  private MethodInfo createMethodInfoForDoRunIteration(final FeatureInfo feature, final Specification currentInstance, final IterationInfo currentIteration, final boolean reportFailures) {
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) throws InvokeException, MultipleInvokeException {
        doRunIteration(feature, currentInstance, currentIteration, reportFailures);
        return null;
      }
    };
    result.setParent(feature.getParent());
    result.setKind(MethodKind.ITERATION_EXECUTION);
    result.setFeature(feature);
    result.setDescription(feature.getDescription());
    for (IMethodInterceptor interceptor : feature.getIterationInterceptors()){
      result.addInterceptor(interceptor);
    }
    return result;
  }

  public void doRunIteration(FeatureInfo feature, Specification currentInstance, IterationInfo currentIteration, boolean reportFailures) throws MultipleInvokeException {
    List<InvokeException> failures = new ArrayList<InvokeException>();
    try {
      runSetup(feature, currentInstance, currentIteration);
      runFeatureMethod(feature, currentInstance, currentIteration);
    } catch (InvokeException e) {
      if (reportFailures){
        supervisor.error(e);
      }else{
        failures.add(e);
      }
    }

    try {
      runCleanup(feature, currentInstance, currentIteration);
    } catch (InvokeException e) {
      if (reportFailures){
        supervisor.error(e);
      }else{
        failures.add(e);
      }
    }

    if (!failures.isEmpty()) {
      throw new MultipleInvokeException(failures);
    }
  }

  protected void runParameterizedFeature(FeatureInfo currentFeature) throws InvokeException, MultipleInvokeException {
    throw new UnsupportedOperationException("This runner cannot run parameterized features");
  }

  private void runInitializer(FeatureInfo feature, final Specification currentInstance) throws InvokeException, MultipleInvokeException {
    runInitializer(spec, feature, currentInstance);
  }

  private void runInitializer(SpecInfo spec, FeatureInfo feature, final Specification currentInstance) throws InvokeException, MultipleInvokeException {
    invoke(feature, NO_CURRENT_ITERATION, currentInstance, this, createMethodInfoForDoRunInitializer(spec, feature, currentInstance), spec);
  }

  private MethodInfo createMethodInfoForDoRunInitializer(final SpecInfo spec, final FeatureInfo feature, final Specification currentInstance) {
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) throws InvokeException, MultipleInvokeException {
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

  public void doRunInitializer(SpecInfo spec, FeatureInfo feature, Specification currentInstance) throws InvokeException, MultipleInvokeException {
    final SpecInfo superSpec = spec.getSuperSpec();
    if (superSpec != null) {
      runInitializer(superSpec, feature, currentInstance);
    }
    invoke(feature, NO_CURRENT_ITERATION, currentInstance, currentInstance, spec.getInitializerMethod());
  }

  private void runSetup(FeatureInfo feature, Specification currentInstance, IterationInfo currentIteration) throws InvokeException, MultipleInvokeException {
    runSetup(spec, feature, currentInstance, currentIteration);
  }

  private void runSetup(SpecInfo spec, FeatureInfo feature, Specification currentInstance, IterationInfo currentIteration) throws InvokeException, MultipleInvokeException {
    if (spec == null) return;
    invoke(feature, currentIteration, currentInstance, this, createMethodInfoForDoRunSetup(spec, feature, currentInstance, currentIteration), spec);
  }

  private MethodInfo createMethodInfoForDoRunSetup(final SpecInfo spec, final FeatureInfo feature, final Specification currentInstance, final IterationInfo currentIteration) {
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) throws InvokeException, MultipleInvokeException {
        doRunSetup(spec, feature, currentInstance, currentIteration);
        return null;
      }
    };
    result.setParent(feature.getParent());
    result.setKind(MethodKind.SETUP);
    result.setFeature(feature);
    result.setDescription(feature.getDescription());
    for (IMethodInterceptor interceptor : spec.getSetupInterceptors()){
      result.addInterceptor(interceptor);
    }
    return result;
  }

  public void doRunSetup(SpecInfo spec, FeatureInfo feature, final Specification currentInstance, IterationInfo currentIteration) throws InvokeException, MultipleInvokeException {
    final SpecInfo superSpec = spec.getSuperSpec();
    if (superSpec != null) {
      runSetup(superSpec, feature, currentInstance, currentIteration);
    }
    for (MethodInfo method : spec.getSetupMethods()) {
      invoke(feature, currentIteration, currentInstance, currentInstance, method);
    }
  }

  private void runFeatureMethod(FeatureInfo feature, final Specification currentInstance, IterationInfo currentIteration) throws InvokeException, MultipleInvokeException {
    invoke(feature, currentIteration, currentInstance, currentInstance, feature.getFeatureMethod(), currentIteration.getDataValues());
  }

  private void runCleanup(FeatureInfo feature, final Specification currentInstance, IterationInfo currentIteration) throws InvokeException, MultipleInvokeException {
    runCleanup(spec, feature, currentInstance, currentIteration);
  }

  private void runCleanup(SpecInfo spec, FeatureInfo feature, final Specification currentInstance, IterationInfo currentIteration) throws InvokeException, MultipleInvokeException {
    invoke(feature, currentIteration, currentInstance, this, createMethodInfoForDoRunCleanup(spec, feature, currentInstance, currentIteration), spec);
  }

  private MethodInfo createMethodInfoForDoRunCleanup(final SpecInfo spec, final FeatureInfo feature, final Specification currentInstance, final IterationInfo currentIteration) {
    MethodInfo result = new MethodInfo() {
      @Override
      public Object invoke(Object target, Object... arguments) throws InvokeException, MultipleInvokeException {
        doRunCleanup(spec, feature, currentInstance, currentIteration);
        return null;
      }
    };
    result.setParent(feature.getParent());
    result.setKind(MethodKind.CLEANUP);
    result.setFeature(feature);
    result.setDescription(feature.getDescription());
    for (IMethodInterceptor interceptor : spec.getCleanupInterceptors()){
      result.addInterceptor(interceptor);
    }
    return result;
  }

  public void doRunCleanup(SpecInfo spec, FeatureInfo feature, final Specification currentInstance, IterationInfo currentIteration) throws InvokeException, MultipleInvokeException {
    if (spec.getIsBottomSpec()) {
      runIterationCleanups(feature, currentIteration);
    }
    for (MethodInfo method : spec.getCleanupMethods()) {
      invoke(feature, currentIteration, currentInstance, currentInstance, method);
    }
    final SpecInfo superSpec = spec.getSuperSpec();
    if (superSpec != null) {
      runCleanup(superSpec, feature, currentInstance, currentIteration);
    }
  }

  private void runIterationCleanups(FeatureInfo currentFeature, IterationInfo currentIteration) {
    for (Runnable cleanup : currentIteration.getCleanups()) {
      try {
        cleanup.run();
      } catch (Throwable t) {
        ErrorInfo error = new ErrorInfo(CollectionUtil.getFirstElement(spec.getCleanupMethods()), t);
        supervisor.error(currentFeature, currentIteration, error);
      }
    }
  }

  private void invoke(FeatureInfo currentFeature, IterationInfo currentIteration, final Specification currentInstance, Object target, MethodInfo method, Object... arguments) throws InvokeException, MultipleInvokeException {
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
    } catch (InvokeException ie) {
      throw ie;
    } catch (MultipleInvokeException ie) {
      throw ie;
    } catch (Throwable t) {
      ErrorInfo error = new ErrorInfo(method, t);
      throw new InvokeException(currentFeature, currentIteration, error);
    }
  }

  protected Object invokeRaw(FeatureInfo currentFeature, IterationInfo currentIteration, Object target, MethodInfo method, Object... arguments) throws InvokeException, MultipleInvokeException {
    try {
      return method.invoke(target, arguments);
    } catch (MultipleInvokeException ie) {
      throw ie;
    } catch (InvokeException ie) {
      throw ie;
    } catch (Throwable t) {
      throw new InvokeException(currentFeature, currentIteration, new ErrorInfo(method, t));
    }
  }

}

