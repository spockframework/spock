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

package org.spockframework.runtime.model;

import org.spockframework.runtime.extension.IDataDriver;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.model.parallel.ExclusiveResource;
import org.spockframework.runtime.model.parallel.ExecutionMode;
import org.spockframework.util.Beta;
import org.spockframework.util.Checks;
import org.spockframework.util.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.util.*;

/**
 * @author Peter Niederwieser
 */
public class FeatureInfo extends SpecElementInfo<SpecInfo, AnnotatedElement> implements ITestTaggable {
  private int declarationOrder; // per spec class
  private int executionOrder;   // per spec inheritance chain

  private final List<String> parameterNames = new ArrayList<>();
  private final List<String> dataVariables = new ArrayList<>();
  private final List<BlockInfo> blocks = new ArrayList<>();
  private final List<IMethodInterceptor> iterationInterceptors = new ArrayList<>();
  private final List<IMethodInterceptor> setupInterceptors = new ArrayList<>();
  private final List<IMethodInterceptor> cleanupInterceptors = new ArrayList<>();
  private final List<IMethodInterceptor> initializerInterceptors = new ArrayList<>();
  private final Map<MethodInfo, List<IMethodInterceptor>> scopedMethodInterceptors = new HashMap<>();

  private final Set<ExclusiveResource> exclusiveResources = new HashSet<>();

  private final Set<TestTag> testTags = new HashSet<>();

  private ExecutionMode executionMode = null;

  private MethodInfo featureMethod;
  private MethodInfo dataProcessorMethod;
  private NameProvider<IterationInfo> iterationNameProvider;
  private IDataDriver dataDriver = IDataDriver.DEFAULT;
  private final List<DataProviderInfo> dataProviders = new ArrayList<>();
  private final IterationFilter iterationFilter = new IterationFilter();

  private final List<FeatureInfo> implyingFeatures = new ArrayList<>();

  private final List<FeatureInfo> impliedFeatures = new ArrayList<>();

  private boolean reportIterations = true;

  private boolean forceParameterized = false;

  public SpecInfo getSpec() {
    return getParent();
  }

  @Override
  public AnnotatedElement getReflection() {
    throw new UnsupportedOperationException("getReflection");
  }

  public int getDeclarationOrder() {
    return declarationOrder;
  }

  public void setDeclarationOrder(int declarationOrder) {
    this.declarationOrder = declarationOrder;
  }

  public int getExecutionOrder() {
    return executionOrder;
  }

  public void setExecutionOrder(int executionOrder) {
    this.executionOrder = executionOrder;
  }

  public List<String> getParameterNames() {
    return parameterNames;
  }

  public void addParameterName(String parameterName) {
    parameterNames.add(parameterName);
  }

  public List<String> getDataVariables() {
    return dataVariables;
  }

  public void addDataVariable(String dataVariable) {
    dataVariables.add(dataVariable);
  }

  public List<BlockInfo> getBlocks() {
    return blocks;
  }

  public void addBlock(BlockInfo block) {
    blocks.add(block);
  }

  /**
   * @since 2.4
   */
  @Beta
  public List<IMethodInterceptor> getSetupInterceptors() {
    return setupInterceptors;
  }

  /**
   * @since 2.4
   */
  @Beta
  public void addSetupInterceptor(IMethodInterceptor interceptor) {
    setupInterceptors.add(interceptor);
  }

  /**
   * @since 2.4
   */
  @Beta
  public List<IMethodInterceptor> getCleanupInterceptors() {
    return cleanupInterceptors;
  }

  /**
   * @since 2.4
   */
  @Beta
  public void addCleanupInterceptor(IMethodInterceptor interceptor) {
    cleanupInterceptors.add(interceptor);
  }

  /**
   * @since 2.4
   */
  @Beta
  public List<IMethodInterceptor> getInitializerInterceptors() {
    return initializerInterceptors;
  }

  /**
   * @since 2.4
   */
  @Beta
  public void addInitializerInterceptor(IMethodInterceptor interceptor) {
    initializerInterceptors.add(interceptor);
  }

  /**
   * Allows to intercept initializer, setup, and cleanup methods in the scope of a single feature.
   * <p>
   * You need to locate the method you want to intercept from the {@link SpecInfo} or its parent and use its {@link MethodInfo} as key.
   * <pre>{@code
   *   visitFeatureAnnotations(MyAnnotation an, FeatureInfo featureInfo) {
   *     featureInfo.addScopedMethodInterceptor(featureInfo.getParent().getInitializerMethod(), invocation -> invocation.proceed());
   *   }
   * }
   * </pre>
   * <p>
   * Only use this if you absolutely must intercept the method invocation itself, otherwise prefer to use one of
   * {@link #addInitializerInterceptor(IMethodInterceptor)},
   * {@link #addSetupInterceptor(IMethodInterceptor)},
   * {@link #addCleanupInterceptor(IMethodInterceptor)}.
   *
   * @since 2.4
   */
  @Beta
  public void addScopedMethodInterceptor(MethodInfo targetMethod, IMethodInterceptor interceptor) {
    Checks.checkArgument(
      targetMethod.getKind().isFeatureScopedFixtureMethod() || targetMethod.getKind() == MethodKind.INITIALIZER,
      () -> "Only feature scoped initializer and fixture methods can be intercepted, but was: " + targetMethod.getKind()
    );
    scopedMethodInterceptors.computeIfAbsent(targetMethod, __ -> new ArrayList<>()).add(interceptor);
  }

  /**
   * @since 2.4
   */
  @Beta
  public List<IMethodInterceptor> getScopedMethodInterceptors(MethodInfo targetMethod) {
    return scopedMethodInterceptors.getOrDefault(targetMethod, Collections.emptyList());
  }

  public List<IMethodInterceptor> getIterationInterceptors() {
    return iterationInterceptors;
  }

  public void addIterationInterceptor(IMethodInterceptor interceptor) {
    iterationInterceptors.add(interceptor);
  }

  public MethodInfo getFeatureMethod() {
    return featureMethod;
  }

  public void setFeatureMethod(MethodInfo method) {
    this.featureMethod = method;
  }

  public MethodInfo getDataProcessorMethod() {
    return dataProcessorMethod;
  }

  public void setDataProcessorMethod(MethodInfo method) {
    this.dataProcessorMethod = method;
  }

  public List<DataProviderInfo> getDataProviders() {
    return dataProviders;
  }

  public void addDataProvider(DataProviderInfo dataProvider) {
    dataProviders.add(dataProvider);
  }

  /**
   * Returns the features that imply this feature.
   * All features are within the same specification hierarchy as this feature.
   * If one of the returned features is going to be executed, this feature is also going to be executed,
   * even if for example a post discovery filter would have filtered out this feature, like IDEs and build
   * tools do if a specific test or a pattern of tests is executed.
   *
   * <p><b>NOTE:</b> This relationship does not imply any ordering constraint. According to configured
   *                 run order and parallel execution settings the features can run in any order or even
   *                 concurrently.
   *
   * @return the features that imply this feature
   */
  @Beta
  public List<FeatureInfo> getImplyingFeatures() {
    return implyingFeatures;
  }

  /**
   * Returns the features this feature implies.
   * All features are within the same specification hierarchy as this feature.
   * If this feature is going to be executed, the returned features are also going to be executed,
   * even if for example a post discovery filter would have filtered them out, like IDEs and build
   * tools do if a specific test or a pattern of tests is executed.
   *
   * <p><b>NOTE:</b> This relationship does not imply any ordering constraint. According to configured
   *                 run order and parallel execution settings the features can run in any order or even
   *                 concurrently.
   *
   * @return the features this feature implies
   */
  @Beta
  public List<FeatureInfo> getImpliedFeatures() {
    return impliedFeatures;
  }

  /**
   * Adds the given feature as implied by this feature.
   * The given feature must be within the same specification hierarchy as this feature.
   * If this feature is going to be executed, the given feature is also going to be executed,
   * even if for example a post discovery filter would have filtered out the given feature,
   * like IDEs and build tools do if a specific test or a pattern of tests is executed.
   *
   * <p><b>NOTE:</b> This relationship does not imply any ordering constraint. According to configured
   *                 run order and parallel execution settings the features can run in any order or even
   *                 concurrently.
   *
   * @param feature a feature that should be implied by this feature
   */
  @Beta
  public void addImpliedFeature(FeatureInfo feature) {
    if (equals(feature)) {
      throw new IllegalArgumentException("Features cannot imply themselves");
    }

    Class<?> otherClass = feature.getParent().getReflection();
    Class<?> clazz = getParent().getReflection();
    if (!otherClass.isAssignableFrom(clazz) && !clazz.isAssignableFrom(otherClass)) {
      throw new IllegalArgumentException("Features can only imply features within the same specification hierarchy");
    }

    impliedFeatures.add(feature);
    feature.implyingFeatures.add(this);
  }

  @Override
  public void addExclusiveResource(ExclusiveResource exclusiveResource) {
    exclusiveResources.add(exclusiveResource);
  }

  @Override
  public Set<ExclusiveResource> getExclusiveResources() {
    return exclusiveResources;
  }

  @Override
  public void setExecutionMode(ExecutionMode executionMode) {
    this.executionMode = executionMode;
  }

  @Override
  public Optional<ExecutionMode> getExecutionMode() {
    return Optional.ofNullable(executionMode);
  }

  public boolean isParameterized() {
    return dataProcessorMethod != null || forceParameterized;
  }


  @Beta
  public boolean isForceParameterized() {
    return forceParameterized;
  }

  /**
   * Forces this feature to behave as if it were parameterized, even if it has no data processor method.
   *
   * @since 2.3
   */
  @Beta
  public void setForceParameterized(boolean forceParameterized) {
    this.forceParameterized = forceParameterized;
  }

  public boolean isReportIterations() {
    return reportIterations;
  }

  public void setReportIterations(boolean flag) {
    reportIterations = flag;
  }

  @Nullable
  public NameProvider<IterationInfo> getIterationNameProvider() {
    return iterationNameProvider;
  }

  public void setIterationNameProvider(NameProvider<IterationInfo> provider) {
    iterationNameProvider = provider;
  }

  public IterationFilter getIterationFilter() {
    return iterationFilter;
  }

  public IDataDriver getDataDriver() {
    return dataDriver;
  }

  public void setDataDriver(IDataDriver dataDriver) {
    this.dataDriver = dataDriver;
  }

  /**
   * Tells if any of the methods associated with this feature has the specified
   * name in bytecode.
   *
   * @param name a method name in bytecode
   * @return <tt>true</tt iff any of the methods associated with this feature
   * has the specified name in bytecode
   */
  public boolean hasBytecodeName(String name) {
    if (featureMethod.hasBytecodeName(name)) return true;
    if (dataProcessorMethod != null && dataProcessorMethod.hasBytecodeName(name)) return true;
    for (DataProviderInfo provider : dataProviders)
      if (provider.getDataProviderMethod().hasBytecodeName(name)) return true;
    return false;
  }

  @Override
  public void addTestTag(TestTag tag) {
    testTags.add(tag);
  }

  @Override
  public Set<TestTag> getTestTags() {
    return testTags;
  }
}
