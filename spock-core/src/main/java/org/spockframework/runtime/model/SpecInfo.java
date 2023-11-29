/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.model;

import org.spockframework.runtime.*;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.model.parallel.*;
import org.spockframework.util.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Comparator.comparingInt;

/**
 * Runtime information about a Spock specification.
 *
 * @author Peter Niederwieser
 */
public class SpecInfo extends SpecElementInfo<NodeInfo, Class<?>> implements IMethodNameMapper {
  private final List<FieldInfo> fields = new ArrayList<>();
  private final List<IMethodInterceptor> setupInterceptors = new ArrayList<>();
  private final List<IMethodInterceptor> cleanupInterceptors = new ArrayList<>();
  private final List<IMethodInterceptor> setupSpecInterceptors = new ArrayList<>();
  private final List<IMethodInterceptor> cleanupSpecInterceptors = new ArrayList<>();
  private final List<IMethodInterceptor> sharedInitializerInterceptors = new ArrayList<>();
  private final List<IMethodInterceptor> initializerInterceptors = new ArrayList<>();

  private final List<IRunListener> listeners = new ArrayList<>();

  private final Set<ExclusiveResource> exclusiveResources = new HashSet<>();

  private ExecutionMode executionMode = null;
  private ExecutionMode childExecutionMode = null;

  private String pkg;
  private String filename;
  private String narrative;

  private SpecInfo superSpec;
  private SpecInfo subSpec;
  private List<SpecInfo> specsTopToBottom;
  private List<SpecInfo> specsBottomToTop;

  private MethodInfo initializerMethod;
  private MethodInfo sharedInitializerMethod;
  private final List<MethodInfo> setupMethods = new ArrayList<>();
  private final List<MethodInfo> cleanupMethods = new ArrayList<>();
  private final List<MethodInfo> setupSpecMethods = new ArrayList<>();
  private final List<MethodInfo> cleanupSpecMethods = new ArrayList<>();

  private final List<FeatureInfo> features = new ArrayList<>();

  public String getPackage() {
    return pkg;
  }

  public void setPackage(String pkg) {
    this.pkg = pkg;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getNarrative() {
    return narrative;
  }

  public void setNarrative(String narrative) {
    this.narrative = narrative;
  }

  public SpecInfo getSuperSpec() {
    return superSpec;
  }

  public void setSuperSpec(SpecInfo superSpec) {
    this.superSpec = superSpec;
  }

  public SpecInfo getSubSpec() {
    return subSpec;
  }

  public void setSubSpec(SpecInfo subSpec) {
    this.subSpec = subSpec;
  }

  public SpecInfo getTopSpec() {
    SpecInfo curr = this;
    while (curr.getSuperSpec() != null)
      curr = curr.getSuperSpec();
    return curr;
  }

  public boolean getIsTopSpec() {
    return superSpec == null;
  }

  public SpecInfo getBottomSpec() {
    SpecInfo curr = this;
    while (curr.getSubSpec() != null)
      curr = curr.getSubSpec();
    return curr;
  }

  public boolean getIsBottomSpec() {
    return subSpec == null;
  }

  public List<SpecInfo> getSpecsCurrentToBottom() {
    List<SpecInfo> topToBottom = getSpecsTopToBottom();
    return topToBottom.subList(topToBottom.indexOf(this), topToBottom.size());
  }

  public List<SpecInfo> getSpecsCurrentToTop() {
    List<SpecInfo> bottomToTop = getSpecsBottomToTop();
    return bottomToTop.subList(bottomToTop.indexOf(this), bottomToTop.size());
  }

  public List<SpecInfo> getSpecsTopToBottom() {
    if (specsTopToBottom == null) {
      specsTopToBottom = collectSpecHierarchy(this::getTopSpec, SpecInfo::getSubSpec);
    }

    return specsTopToBottom;
  }

  public List<SpecInfo> getSpecsBottomToTop() {
    if (specsBottomToTop == null) {
      specsBottomToTop = collectSpecHierarchy(this::getBottomSpec, SpecInfo::getSuperSpec);
    }

    return specsBottomToTop;
  }

  private List<SpecInfo> collectSpecHierarchy(Supplier<SpecInfo> start, Function<SpecInfo, SpecInfo> next) {
    List<SpecInfo> specs = new ArrayList<>();
    SpecInfo curr = start.get();
    while (curr != null) {
      specs.add(curr);
      curr = next.apply(curr);
    }
    return specs;
  }

  public MethodInfo getInitializerMethod() {
    return initializerMethod;
  }

  public List<MethodInfo> getAllInitializerMethods() {
    return collectAll(SpecInfo::getInitializerMethod);
  }

  public void setInitializerMethod(MethodInfo initializerMethod) {
    this.initializerMethod = initializerMethod;
  }

  public MethodInfo getSharedInitializerMethod() {
    return sharedInitializerMethod;
  }

  public List<MethodInfo> getAllSharedInitializerMethods() {
    return collectAll(SpecInfo::getSharedInitializerMethod);
  }

  public void setSharedInitializerMethod(MethodInfo sharedInitializerMethod) {
    this.sharedInitializerMethod = sharedInitializerMethod;
  }

  public List<MethodInfo> getSetupMethods() {
    return setupMethods;
  }

  public List<MethodInfo> getAllSetupMethods() {
    return collectAllFromIterable(SpecInfo::getSetupMethods);
  }

  public void addSetupMethod(MethodInfo setupMethod) {
    setupMethods.add(setupMethod);
  }

  public List<MethodInfo> getCleanupMethods() {
    return cleanupMethods;
  }

  public List<MethodInfo> getAllCleanupMethods() {
    return collectAllFromIterable(SpecInfo::getCleanupMethods);
  }

  public void addCleanupMethod(MethodInfo cleanupMethod) {
    cleanupMethods.add(cleanupMethod);
  }

  public List<MethodInfo> getSetupSpecMethods() {
    return setupSpecMethods;
  }

  public List<MethodInfo> getAllSetupSpecMethods() {
    return collectAllFromIterable(SpecInfo::getSetupSpecMethods);
  }

  public void addSetupSpecMethod(MethodInfo setupSpecMethod) {
    setupSpecMethods.add(setupSpecMethod);
  }

  public List<MethodInfo> getCleanupSpecMethods() {
    return cleanupSpecMethods;
  }

  public List<MethodInfo> getAllCleanupSpecMethods() {
    return collectAllFromIterable(SpecInfo::getCleanupSpecMethods);
  }

  public void addCleanupSpecMethod(MethodInfo cleanupSpecMethod) {
    cleanupSpecMethods.add(cleanupSpecMethod);
  }

  public List<MethodInfo> getFixtureMethods() {
    List<MethodInfo> result = new ArrayList<>();
    result.addAll(setupSpecMethods);
    result.addAll(setupMethods);
    result.addAll(cleanupMethods);
    result.addAll(cleanupSpecMethods);
    return result;
  }

  public List<MethodInfo> getAllFixtureMethods() {
    return collectAllFromIterable(SpecInfo::getFixtureMethods);
  }

  public List<FieldInfo> getFields() {
    return fields;
  }

  public List<FieldInfo> getAllFields() {
    return collectAllFromIterable(SpecInfo::getFields);
  }

  public void addField(FieldInfo field) {
    fields.add(field);
  }

  public List<FeatureInfo> getFeatures() {
    return features;
  }

  public List<FeatureInfo> getAllFeatures() {
    return collectAllFromIterable(SpecInfo::getFeatures);
  }

  public List<FeatureInfo> getAllFeaturesInExecutionOrder() {
    List<FeatureInfo> result = getAllFeatures();
    result.sort(comparingInt(FeatureInfo::getExecutionOrder));
    return result;
  }

  private <T> List<T> collectAll(Function<SpecInfo, T> getter) {
    List<T> result = new ArrayList<>();
    collectAll(result, getter, List::add);
    return result;
  }

  private <T> List<T> collectAllFromIterable(Function<SpecInfo, Iterable<T>> getter) {
    List<T> result = new ArrayList<>();
    collectAll(result, getter, (a, i) -> i.forEach(a::add));
    return result;
  }

  private <T,U> void collectAll(List<T> accumulator, Function<SpecInfo, U> getter, BiConsumer<List<T>, U> combiner) {
    if (superSpec != null) {
      superSpec.collectAll(accumulator, getter, combiner);
    }
    combiner.accept(accumulator, getter.apply(this));
  }

  public void addFeature(FeatureInfo feature) {
    features.add(feature);
  }

  public List<IMethodInterceptor> getSetupInterceptors() {
    return setupInterceptors;
  }

  public void addSetupInterceptor(IMethodInterceptor interceptor) {
    setupInterceptors.add(interceptor);
  }

  public List<IMethodInterceptor> getCleanupInterceptors() {
    return cleanupInterceptors;
  }

  public void addCleanupInterceptor(IMethodInterceptor interceptor) {
    cleanupInterceptors.add(interceptor);
  }

  public List<IMethodInterceptor> getSetupSpecInterceptors() {
    return setupSpecInterceptors;
  }

  public void addSetupSpecInterceptor(IMethodInterceptor interceptor) {
    setupSpecInterceptors.add(interceptor);
  }

  public List<IMethodInterceptor> getCleanupSpecInterceptors() {
    return cleanupSpecInterceptors;
  }

  public void addCleanupSpecInterceptor(IMethodInterceptor interceptor) {
    cleanupSpecInterceptors.add(interceptor);
  }

  public List<IMethodInterceptor> getSharedInitializerInterceptors() {
    return sharedInitializerInterceptors;
  }

  public void addSharedInitializerInterceptor(IMethodInterceptor interceptor) {
    sharedInitializerInterceptors.add(interceptor);
  }

  public List<IMethodInterceptor> getInitializerInterceptors() {
    return initializerInterceptors;
  }

  public void addInitializerInterceptor(IMethodInterceptor interceptor) {
    initializerInterceptors.add(interceptor);
  }

  public List<IRunListener> getListeners() {
    return listeners;
  }

  public void addListener(IRunListener listener) {
    listeners.add(listener);
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

  public Optional<ExecutionMode> getChildExecutionMode() {
    return Optional.ofNullable(childExecutionMode);
  }

  public void setChildExecutionMode(ExecutionMode childExecutionMode) {
    this.childExecutionMode = childExecutionMode;
  }

  public void filterFeatures(final IFeatureFilter filter) {
    for (FeatureInfo feature : getAllFeatures()) {
      if (!filter.matches(feature))
        feature.setExcluded(true);
    }
  }

  public void sortFeatures(final IFeatureSortOrder order) {
    List<FeatureInfo> features = getAllFeatures();
    features.sort(order);
    for (int i = 0; i < features.size(); i++)
      features.get(i).setExecutionOrder(i);
  }

  @Override
  public boolean isInitializerOrFixtureMethod(String className, String methodName) {
    if (!InternalIdentifiers.INITIALIZER_AND_FIXTURE_METHODS.contains(methodName))
      return false;

    for (SpecInfo spec : getSpecsBottomToTop())
      if (spec.getReflection().getName().equals(className))
        return true;

    return false;
  }

  @Override
  public String toFeatureName(String methodName) {
    for (FeatureInfo feature : getAllFeatures())
      if (feature.hasBytecodeName(methodName))
        return feature.getName();
    return methodName;
  }
}
