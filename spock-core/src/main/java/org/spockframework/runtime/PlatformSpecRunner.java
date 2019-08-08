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

import org.junit.platform.engine.support.hierarchical.Node;
import org.junit.runner.Description;

import static org.spockframework.runtime.RunStatus.*;

/**
 * Executes a single Spec. Notifies its supervisor about overall execution
 * progress and every invocation of Spec code.
 * Supervisor also determines the error strategy.
 *
 * @author Peter Niederwieser
 */
public class PlatformSpecRunner {

  protected final IRunSupervisor supervisor;
  protected int runStatus = OK;

  public PlatformSpecRunner(IRunSupervisor supervisor) {
    this.supervisor = supervisor;
  }

  public int run(SpockExecutionContext context) {
    // Sometimes a spec run is requested even though the spec has been excluded
    // (e.g. if JUnit is in control). In such a case, the best thing we can do
    // is to treat the spec as skipped.
    SpecInfo spec = context.getSpec();
    if (spec.isExcluded() || spec.isSkipped()) {
      supervisor.specSkipped(spec);
      return OK;
    }

    context = runSharedSpec(context);
//    runSpec(context);

    return resetStatus(SPEC);
  }

  SpockExecutionContext runSharedSpec(SpockExecutionContext context) {
    context = createSpecInstance(context, true);
    runSharedInitializer(context);
    return context;
  }

  public void runSpec(SpockExecutionContext context, Runnable specRunner) {
    if (runStatus != OK) return;

    SpecInfo spec = context.getSpec();
    supervisor.beforeSpec(spec);
    invoke(context, this, createMethodInfoForDoRunSpec(context, specRunner));
    supervisor.afterSpec(spec);
  }

  private MethodInfo createMethodInfoForDoRunSpec(SpockExecutionContext context, Runnable specRunner) {
    MethodInfo result = new MethodInfo((Object target, Object... arguments) -> {
        specRunner.run();
        return null;
      }
    );
    SpecInfo spec = context.getSpec();
    result.setParent(spec);
    result.setKind(MethodKind.SPEC_EXECUTION);
    for (IMethodInterceptor interceptor : spec.getInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  public void doRunSpec(SpockExecutionContext context) {
    runSetupSpec(context);
    runFeatures(context);
    runCleanupSpec(context);
  }

  SpockExecutionContext createSpecInstance(SpockExecutionContext context, boolean shared) {
    if (runStatus != OK) return context;

    Specification instance;
    try {
        instance = (Specification) context.getSpec().getReflection().newInstance();
    } catch (Throwable t) {
      throw new InternalSpockError("Failed to instantiate spec '%s'", t).withArgs(context.getSpec().getName());
    }


    context = context.withCurrentInstance(instance);
    getSpecificationContext(context).setCurrentSpec(context.getSpec());
    if (shared) {
      context = context.withSharedInstance(instance);
    }
    getSpecificationContext(context).setSharedInstance(context.getSharedInstance());
    return context;
  }

  private void runSharedInitializer(SpockExecutionContext context) {
    runSharedInitializer(context, context.getSpec());
  }

  private void runSharedInitializer(SpockExecutionContext context, SpecInfo spec) {
    if (spec == null) return;
    invoke(context, this, createMethodInfoForDoRunSharedInitializer(context, spec), spec);
  }

  private MethodInfo createMethodInfoForDoRunSharedInitializer(SpockExecutionContext context, final SpecInfo spec) {
    MethodInfo result = new MethodInfo((Object target, Object... arguments) -> {
        doRunSharedInitializer(context, spec);
        return null;
      }
    );
    result.setParent(spec);
    result.setKind(MethodKind.SHARED_INITIALIZER);
    for (IMethodInterceptor interceptor : spec.getSharedInitializerInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  public void doRunSharedInitializer(SpockExecutionContext context, SpecInfo spec) {
    runSharedInitializer(context, spec.getSuperSpec());
    if (runStatus != OK) return;
    invoke(context, context.getCurrentInstance(), spec.getSharedInitializerMethod());
  }

  void runSetupSpec(SpockExecutionContext context) {
    supervisor.beforeSpec(context.getSpec());
    runSetupSpec(context, context.getSpec());
  }

  private void runSetupSpec(SpockExecutionContext context, SpecInfo spec) {
    if (spec == null) return;
    invoke(context, this, createMethodInfoForDoRunSetupSpec(context, spec), spec);
  }

  private MethodInfo createMethodInfoForDoRunSetupSpec(SpockExecutionContext context, final SpecInfo spec) {
    MethodInfo result = new MethodInfo((Object target, Object... arguments) -> {
        doRunSetupSpec(context, spec);
        return null;
      }
    );
    result.setParent(spec);
    result.setKind(MethodKind.SETUP_SPEC);
    for (IMethodInterceptor interceptor : spec.getSetupSpecInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  public void doRunSetupSpec(SpockExecutionContext context, SpecInfo spec) {
    runSetupSpec(context, spec.getSuperSpec());
    for (MethodInfo method : spec.getSetupSpecMethods()) {
      if (runStatus != OK) return;
      invoke(context, context.getCurrentInstance(), method);
    }
  }

  private void runFeatures(SpockExecutionContext context) {
    for (FeatureInfo feature : context.getSpec().getAllFeaturesInExecutionOrder()) {
      if (resetStatus(FEATURE) != OK) return;
//      runFeature(context.withCurrentFeature(feature));
    }
  }

  void runCleanupSpec(SpockExecutionContext context) {
    runCleanupSpec(context.withCurrentInstance(context.getSharedInstance()), context.getSpec());
    supervisor.afterSpec(context.getSpec());
  }

  private void runCleanupSpec(SpockExecutionContext context, SpecInfo spec) {
    if (spec == null) return;
    invoke(context,this, createMethodForDoRunCleanupSpec(context, spec), spec);
  }

  private MethodInfo createMethodForDoRunCleanupSpec(SpockExecutionContext context, final SpecInfo spec) {
    MethodInfo result = new MethodInfo((Object target, Object... arguments) -> {
        doRunCleanupSpec(context, spec);
        return null;
      }
    );
    result.setParent(spec);
    result.setKind(MethodKind.CLEANUP_SPEC);
    for (IMethodInterceptor interceptor : spec.getCleanupSpecInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  public void doRunCleanupSpec(SpockExecutionContext context, SpecInfo spec) {
    for (MethodInfo method : spec.getCleanupSpecMethods()) {
      if (action(runStatus) == ABORT) return;
      invoke(context, context.getCurrentInstance(), method);
    }
    runCleanupSpec(context, spec.getSuperSpec());
  }

  public void runFeature(SpockExecutionContext context, Runnable feature) {
    if (runStatus != OK) return;

    FeatureInfo currentFeature = context.getCurrentFeature();
    if (currentFeature.isExcluded()) return;

    if (currentFeature.isSkipped()) {
      supervisor.featureSkipped(currentFeature); // todo notify in node isSkipped
      return;
    }

    supervisor.beforeFeature(currentFeature);
    invoke(context, this, createMethodInfoForDoRunFeature(context, feature));
    supervisor.afterFeature(currentFeature);
  }

  private MethodInfo createMethodInfoForDoRunFeature(SpockExecutionContext context, Runnable feature) {
    FeatureInfo currentFeature = context.getCurrentFeature();
    MethodInfo result = new MethodInfo((Object target, Object... arguments) -> {
        feature.run();
        return null;
      }
    );
    result.setParent(currentFeature.getParent());
    result.setKind(MethodKind.FEATURE_EXECUTION);
    result.setFeature(currentFeature);
    for (IMethodInterceptor interceptor : currentFeature.getInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  public void doRunFeature(SpockExecutionContext context) { // TODO Remove
    FeatureInfo currentFeature = context.getCurrentFeature();
    currentFeature.setIterationNameProvider(new SafeIterationNameProvider(currentFeature.getIterationNameProvider()));
//    if (currentFeature.isParameterized())
//      runParameterizedFeature(context);
//    else runSimpleFeature(context);
  }

  void runIteration(SpockExecutionContext context, IterationInfo iterationInfo, Runnable runnable) {
    if (runStatus != OK) return;

    context = context.withCurrentIteration(iterationInfo);
    getSpecificationContext(context).setCurrentIteration(iterationInfo);

    supervisor.beforeIteration(iterationInfo);
    invoke(context, this, createMethodInfoForDoRunIteration(context, runnable));
    supervisor.afterIteration(iterationInfo);

    getSpecificationContext(context).setCurrentIteration(null); // TODO check if we really need to null here
  }

  IterationInfo createIterationInfo(SpockExecutionContext context, Object[] dataValues, int estimatedNumIterations) {
    FeatureInfo currentFeature = context.getCurrentFeature();
    IterationInfo result = new IterationInfo(currentFeature, dataValues, estimatedNumIterations);
    String iterationName = currentFeature.getIterationNameProvider().getName(result);
    result.setName(iterationName);
    Description description = Description.createTestDescription(context.getSpec().getReflection(),
        iterationName, currentFeature.getFeatureMethod().getAnnotations());
    return result;
  }

  private MethodInfo createMethodInfoForDoRunIteration(SpockExecutionContext context, Runnable runnable) {
    FeatureInfo currentFeature = context.getCurrentFeature();
    MethodInfo result = new MethodInfo((Object target, Object... arguments) -> {
        runnable.run();
        return null;
      }
    );
    result.setParent(currentFeature.getParent());
    result.setKind(MethodKind.ITERATION_EXECUTION);
    result.setFeature(currentFeature);
    result.setIteration(context.getCurrentIteration());
    for (IMethodInterceptor interceptor : currentFeature.getIterationInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  public void doRunIteration(SpockExecutionContext context) {
    runSetup(context);
    runFeatureMethod(context);
    runCleanup(context);
  }

  protected int resetStatus(int scope) {
    if (scope(runStatus) <= scope) runStatus = OK;
    return runStatus;
  }

  void runParameterizedFeature(SpockExecutionContext context, Node.DynamicTestExecutor dynamicTestExecutor) throws InterruptedException  {
    throw new UnsupportedOperationException("This runner cannot run parameterized features");
  }

  void runInitializer(SpockExecutionContext context) {
    runInitializer(context, context.getSpec());
  }

  private void runInitializer(SpockExecutionContext context, SpecInfo spec) {
    if (spec == null) return;
    invoke(context, this, createMethodInfoForDoRunInitializer(context, spec), spec);
  }

  private MethodInfo createMethodInfoForDoRunInitializer(SpockExecutionContext context, final SpecInfo spec) {
    FeatureInfo currentFeature = context.getCurrentFeature();
    MethodInfo result = new MethodInfo((Object target, Object... arguments) -> {
        doRunInitializer(context, spec);
        return null;
      }
    );
    result.setParent(currentFeature.getParent());
    result.setKind(MethodKind.INITIALIZER);
    result.setFeature(currentFeature);
    for (IMethodInterceptor interceptor : spec.getInitializerInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  public void doRunInitializer(SpockExecutionContext context, SpecInfo spec) {
    runInitializer(context, spec.getSuperSpec());
    if (runStatus != OK) return;
    invoke(context, context.getCurrentInstance(), spec.getInitializerMethod());
  }

  void runSetup(SpockExecutionContext context) {
    runSetup(context, context.getSpec());
  }

  private void runSetup(SpockExecutionContext context, SpecInfo spec) {
    if (spec == null) return;
    invoke(context, this, createMethodInfoForDoRunSetup(context, spec), spec);
  }

  private MethodInfo createMethodInfoForDoRunSetup(SpockExecutionContext context, final SpecInfo spec) {
    FeatureInfo currentFeature = context.getCurrentFeature();
    MethodInfo result = new MethodInfo((Object target, Object... arguments) -> {
        doRunSetup(context, spec);
        return null;
      }
    );
    result.setParent(currentFeature.getParent());
    result.setKind(MethodKind.SETUP);
    result.setFeature(currentFeature);
    result.setIteration(context.getCurrentIteration());
    for (IMethodInterceptor interceptor : spec.getSetupInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  private void doRunSetup(SpockExecutionContext context, SpecInfo spec) {
    runSetup(context, spec.getSuperSpec());
    for (MethodInfo method : spec.getSetupMethods()) {
      if (runStatus != OK) return;
      method.setFeature(context.getCurrentFeature());
      invoke(context, context.getCurrentInstance(), method);
    }
  }

  void runFeatureMethod(SpockExecutionContext context) {
    if (runStatus != OK) return;

    MethodInfo featureIteration = new MethodInfo(context.getCurrentFeature().getFeatureMethod());
    featureIteration.setIteration(context.getCurrentIteration());
    invoke(context, context.getCurrentInstance(), featureIteration, context.getCurrentIteration().getDataValues());
  }

  void runCleanup(SpockExecutionContext context) {
    runCleanup(context, context.getSpec());
  }

  private void runCleanup(SpockExecutionContext context, SpecInfo spec) {
    if (spec == null) return;
    invoke(context, this, createMethodInfoForDoRunCleanup(context, spec), spec);
  }

  private MethodInfo createMethodInfoForDoRunCleanup(SpockExecutionContext context, final SpecInfo spec) {
    FeatureInfo currentFeature = context.getCurrentFeature();
    MethodInfo result = new MethodInfo((Object target, Object... arguments) -> {
        doRunCleanup(context, spec);
        return null;
      }
    );
    result.setParent(currentFeature.getParent());
    result.setKind(MethodKind.CLEANUP);
    result.setFeature(currentFeature);
    result.setIteration(context.getCurrentIteration());
    for (IMethodInterceptor interceptor : spec.getCleanupInterceptors())
      result.addInterceptor(interceptor);
    return result;
  }

  private void doRunCleanup(SpockExecutionContext context, SpecInfo spec) {
    if (spec.getIsBottomSpec()) {
      runIterationCleanups(context);
      if (action(runStatus) == ABORT) return;
    }
    for (MethodInfo method : spec.getCleanupMethods()) {
      if (action(runStatus) == ABORT) return;
      invoke(context, context.getCurrentInstance(), method);
    }
    runCleanup(context, spec.getSuperSpec());
  }

  private void runIterationCleanups(SpockExecutionContext context) {
    for (Runnable cleanup : context.getCurrentIteration().getCleanups()) {
      if (action(runStatus) == ABORT) return;
      try {
        cleanup.run();
      } catch (Throwable t) {
        ErrorInfo error = new ErrorInfo(CollectionUtil.getFirstElement(context.getSpec().getCleanupMethods()), t);
        runStatus = supervisor.error(error);
      }
    }
  }

  protected void invoke(SpockExecutionContext context, Object target, MethodInfo method, Object... arguments) {
    if (method == null || method.isExcluded()) {
      return;
    }
    // fast lane
    if (method.getInterceptors().isEmpty()) {
      invokeRaw(target, method, arguments);
      return;
    }

    // slow lane
    MethodInvocation invocation = new MethodInvocation(context.getCurrentFeature(),
      context.getCurrentIteration(), context.getSharedInstance(), context.getCurrentInstance(), target, method, arguments);
    try {
      invocation.proceed();
    } catch (Throwable throwable) {
      ErrorInfo error = new ErrorInfo(method, throwable);
      supervisor.error(error);
      ExceptionUtil.sneakyThrow(throwable);
    }
  }

  protected Object invokeRaw(Object target, MethodInfo method, Object... arguments) {
    try {
      return method.invoke(target, arguments);
    } catch (Throwable throwable) {
      ErrorInfo error = new ErrorInfo(method, throwable);
      supervisor.error(error);
      ExceptionUtil.sneakyThrow(throwable);
      return null; // never happens tm
    }
  }


  protected SpecificationContext getSpecificationContext(SpockExecutionContext context) {
    return (SpecificationContext) context.getCurrentInstance().getSpecificationContext();
  }
}
