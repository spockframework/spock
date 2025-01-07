/*
 * Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.runtime;

import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.MethodInvocation;
import org.spockframework.runtime.model.*;
import org.spockframework.util.CollectionUtil;
import org.spockframework.util.InternalSpockError;
import spock.lang.Specification;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOfRange;
import static org.spockframework.runtime.model.MethodInfo.MISSING_ARGUMENT;

/**
 * Executes a single Spec. Notifies its supervisor about overall execution
 * progress and every invocation of Spec code.
 * Supervisor also determines the error strategy.
 *
 * @author Peter Niederwieser
 */
public class PlatformSpecRunner {

  protected final IRunSupervisor supervisor;

  public PlatformSpecRunner(IRunSupervisor supervisor) {
    this.supervisor = supervisor;
  }

  SpockExecutionContext runSharedSpec(SpockExecutionContext context) {
    context = createSpecInstance(context, true);
    runSharedInitializer(context);
    return context;
  }

  public void runSpec(SpockExecutionContext context, Runnable specRunner) {
    if (context.getErrorInfoCollector().hasErrors()) return;

    SpecInfo spec = context.getSpec();
    supervisor.beforeSpec(spec);
    invoke(context, this, createMethodInfoForDoRunSpec(context, specRunner));
    supervisor.afterSpec(spec);
    runCloseContextStoreProvider(context, MethodKind.CLEANUP_SPEC);
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
    spec.getInterceptors().forEach(result::addInterceptor);
    return result;
  }

  SpockExecutionContext createSpecInstance(SpockExecutionContext context, boolean shared) {
    if (context.getErrorInfoCollector().hasErrors()) return context;

    Specification instance;
    try {
        instance = (Specification) context.getSpec().getReflection().newInstance();
    } catch (Throwable t) {
      throw new InternalSpockError("Failed to instantiate spec '%s'", t).withArgs(context.getSpec().getName());
    }


    context = context.withChildStoreProvider().withCurrentInstance(instance);
    getSpecificationContext(context).setCurrentSpec(context.getSpec());

    // this is either for the shared spec instance or the current iteration
    getSpecificationContext(context).setStoreProvider(context.getStoreProvider());
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
    spec.getSharedInitializerInterceptors().forEach(result::addInterceptor);
    return result;
  }

  public void doRunSharedInitializer(SpockExecutionContext context, SpecInfo spec) {
    runSharedInitializer(context, spec.getSuperSpec());
    if (context.getErrorInfoCollector().hasErrors()) return;
    invoke(context, context.getCurrentInstance(), spec.getSharedInitializerMethod());
  }

  void runSetupSpec(SpockExecutionContext context) {
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
    spec.getSetupSpecInterceptors().forEach(result::addInterceptor);
    return result;
  }

  public void doRunSetupSpec(SpockExecutionContext context, SpecInfo spec) {
    runSetupSpec(context, spec.getSuperSpec());
    for (MethodInfo method : spec.getSetupSpecMethods()) {
      if (context.getErrorInfoCollector().hasErrors()) return;
      invoke(context, context.getCurrentInstance(), method);
    }
  }

  void runCleanupSpec(SpockExecutionContext context) {
    runCleanupSpec(context.withCurrentInstance(context.getSharedInstance()), context.getSpec());
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
    spec.getCleanupSpecInterceptors().forEach(result::addInterceptor);
    return result;
  }

  public void doRunCleanupSpec(SpockExecutionContext context, SpecInfo spec) {
    for (MethodInfo method : spec.getCleanupSpecMethods()) {
      invoke(context, context.getCurrentInstance(), method);
    }
    runCleanupSpec(context, spec.getSuperSpec());
  }

  public void runFeature(SpockExecutionContext context, Runnable feature) {
    if (context.getErrorInfoCollector().hasErrors()) return;

    FeatureInfo currentFeature = context.getCurrentFeature();
    if (currentFeature.isExcluded()) return;

    if (currentFeature.isSkipped()) {
      throw new InternalSpockError("Invalid state, feature is executed although it should have been skipped");
    }

    // at this point the specification context is the one of the shared instance,
    // so the current feature cannot be set as features can run in parallel
    // so getting the current feature from the specification context would not properly work

    supervisor.beforeFeature(currentFeature);
    invoke(context, this, createMethodInfoForDoRunFeature(context, feature));
    supervisor.afterFeature(currentFeature);

    runCloseContextStoreProvider(context, MethodKind.CLEANUP);
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
    currentFeature.getInterceptors().forEach(result::addInterceptor);
    return result;
  }

  void runIteration(SpockExecutionContext context, IterationInfo iterationInfo, Runnable runnable) {
    if (context.getErrorInfoCollector().hasErrors()) return;

    context = context.withCurrentIteration(iterationInfo);
    getSpecificationContext(context).setCurrentIteration(iterationInfo);

    supervisor.beforeIteration(iterationInfo);
    invoke(context, this, createMethodInfoForDoRunIteration(context, runnable));
    supervisor.afterIteration(iterationInfo);
    runCloseContextStoreProvider(context, MethodKind.CLEANUP);
  }

  IterationInfo createIterationInfo(SpockExecutionContext context, int iterationIndex, Object[] args, int estimatedNumIterations) {
    FeatureInfo currentFeature = context.getCurrentFeature();
    Object[] dataValues = copyOfRange(args, 0, currentFeature.getDataVariables().size());
    IterationInfo result = new IterationInfo(currentFeature, iterationIndex, dataValues, estimatedNumIterations);
    result.setName(currentFeature.getName());
    String iterationName = currentFeature.getIterationNameProvider().getName(result);
    result.setDisplayName(iterationName);
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
    currentFeature.getIterationInterceptors().forEach(result::addInterceptor);
    return result;
  }

  void runParameterizedFeature(SpockExecutionContext context, ParameterizedFeatureChildExecutor childExecutor) throws InterruptedException  {
    throw new UnsupportedOperationException("This runner cannot run parameterized features");
  }

  void runInitializer(SpockExecutionContext context) {
    getSpecificationContext(context).setCurrentIteration(context.getCurrentIteration());
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
    if (spec.getIsBottomSpec()) {
      currentFeature.getInitializerInterceptors().forEach(result::addInterceptor);
    }
    spec.getInitializerInterceptors().forEach(result::addInterceptor);
    return result;
  }

  public void doRunInitializer(SpockExecutionContext context, SpecInfo spec) {
    runInitializer(context, spec.getSuperSpec());
    if (context.getErrorInfoCollector().hasErrors()) return;
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
    if (spec.getIsBottomSpec()) {
      currentFeature.getSetupInterceptors().forEach(result::addInterceptor);
    }
    spec.getSetupInterceptors().forEach(result::addInterceptor);
    return result;
  }

  private void doRunSetup(SpockExecutionContext context, SpecInfo spec) {
    runSetup(context, spec.getSuperSpec());
    for (MethodInfo method : spec.getSetupMethods()) {
      if (context.getErrorInfoCollector().hasErrors()) return;
      method.setFeature(context.getCurrentFeature());
      invoke(context, context.getCurrentInstance(), method);
    }
  }

  void runFeatureMethod(SpockExecutionContext context) {
    if (context.getErrorInfoCollector().hasErrors()) return;

    MethodInfo featureIteration = new MethodInfo(context.getCurrentFeature().getFeatureMethod());
    featureIteration.setIteration(context.getCurrentIteration());

    Object[] dataValues = context.getCurrentIteration().getDataValues();
    invoke(context, context.getCurrentInstance(), featureIteration, dataValues);
    getSpecificationContext(context).setCurrentBlock(null);
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
    if (spec.getIsBottomSpec()) {
      currentFeature.getCleanupInterceptors().forEach(result::addInterceptor);
    }
    spec.getCleanupInterceptors().forEach(result::addInterceptor);
    return result;
  }

  private void doRunCleanup(SpockExecutionContext context, SpecInfo spec) {
    if (spec.getIsBottomSpec()) {
      runIterationCleanups(context);
    }
    for (MethodInfo method : spec.getCleanupMethods()) {
      invoke(context, context.getCurrentInstance(), method);
    }
    runCleanup(context, spec.getSuperSpec());
  }

  private void runIterationCleanups(SpockExecutionContext context) {
    for (Runnable cleanup : context.getCurrentIteration().getCleanups()) {
      try {
        cleanup.run();
      } catch (Throwable t) {
        ErrorInfo error = new ErrorInfo(CollectionUtil.getFirstElement(context.getSpec().getCleanupMethods()), t, ErrorContext.from(getSpecificationContext(context)));
        supervisor.error(context.getErrorInfoCollector(), error);
      }
    }
  }

  void runCloseContextStoreProvider(SpockExecutionContext context, MethodKind kind) {
    MethodInfo methodInfo = createMethodInfoForCloseContextStoreProvider(context, kind);
    invokeRaw(context, context.getStoreProvider(), methodInfo);
  }

  private MethodInfo createMethodInfoForCloseContextStoreProvider(SpockExecutionContext context, MethodKind kind) {
    MethodInfo result = new MethodInfo((Object target, Object... arguments) -> {
        context.getStoreProvider().close();
        return null;
      }
    );
    result.setKind(kind);
    return result;
  }

  protected void invoke(SpockExecutionContext context, Object target, MethodInfo method, Object... arguments) {
    if (method == null || method.isExcluded()) {
      return;
    }

    Object[] methodArguments;
    int parameterCount = method.getReflection() == null ? arguments.length : method.getReflection().getParameterCount();
    if (arguments.length == parameterCount) {
      methodArguments = arguments;
    } else {
      methodArguments = new Object[parameterCount];
      arraycopy(arguments, 0, methodArguments, 0, arguments.length);
      Arrays.fill(methodArguments, arguments.length, parameterCount, MISSING_ARGUMENT);
    }

    List<IMethodInterceptor> scopedInterceptors = Collections.emptyList();
    if (context.getCurrentFeature() != null) {
      scopedInterceptors = context.getCurrentFeature().getScopedMethodInterceptors(method);
    }

    // fast lane
    if (method.getInterceptors().isEmpty() && scopedInterceptors.isEmpty()) {
      invokeRaw(context, target, method, methodArguments);
      return;
    }

    // slow lane
    MethodInvocation invocation = new MethodInvocation(context.getCurrentFeature(),
      context.getCurrentIteration(), context.getStoreProvider(), context.getSharedInstance(), context.getCurrentInstance(), target, method, scopedInterceptors, methodArguments);
    try {
      invocation.proceed();
    } catch (Throwable throwable) {
      ErrorInfo error = new ErrorInfo(method, throwable, ErrorContext.from(getSpecificationContext(context)));
      supervisor.error(context.getErrorInfoCollector(), error);
    }
  }

  protected Object invokeRaw(SpockExecutionContext context, Object target, MethodInfo method, Object... arguments) {
    try {
      return method.invoke(target, arguments);
    } catch (Throwable throwable) {
      supervisor.error(context.getErrorInfoCollector(), new ErrorInfo(method, throwable, ErrorContext.from(getSpecificationContext(context))));
      return null;
    }
  }


  protected SpecificationContext getSpecificationContext(SpockExecutionContext context) {
    return (SpecificationContext) context.getCurrentInstance().getSpecificationContext();
  }
}
