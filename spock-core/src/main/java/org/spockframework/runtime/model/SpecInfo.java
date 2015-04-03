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

package org.spockframework.runtime.model;

import java.util.*;

import org.spockframework.runtime.*;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.util.*;

/**
 * Runtime information about a Spock specification.
 * 
 * @author Peter Niederwieser
 */
public class SpecInfo extends SpecElementInfo<NodeInfo, Class<?>> implements IMethodNameMapper {
  private final List<FieldInfo> fields = new ArrayList<FieldInfo>();
  private final List<IMethodInterceptor> setupInterceptors = new ArrayList<IMethodInterceptor>();
  private final List<IMethodInterceptor> cleanupInterceptors = new ArrayList<IMethodInterceptor>();
  private final List<IMethodInterceptor> setupSpecInterceptors = new ArrayList<IMethodInterceptor>();
  private final List<IMethodInterceptor> cleanupSpecInterceptors = new ArrayList<IMethodInterceptor>();
  private final List<IMethodInterceptor> sharedInitializerInterceptors = new ArrayList<IMethodInterceptor>();
  private final List<IMethodInterceptor> initializerInterceptors = new ArrayList<IMethodInterceptor>();

  private final List<IRunListener> listeners = new ArrayList<IRunListener>();

  private String pkg;
  private String filename;
  private String narrative;

  private SpecInfo superSpec;
  private SpecInfo subSpec;
  private List<SpecInfo> specsTopToBottom;
  private List<SpecInfo> specsBottomToTop;

  private MethodInfo initializerMethod;
  private MethodInfo sharedInitializerMethod;
  private final List<MethodInfo> setupMethods = new ArrayList<MethodInfo>();
  private final List<MethodInfo> cleanupMethods = new ArrayList<MethodInfo>();
  private final List<MethodInfo> setupSpecMethods = new ArrayList<MethodInfo>();
  private final List<MethodInfo> cleanupSpecMethods = new ArrayList<MethodInfo>();

  private final List<FeatureInfo> features = new ArrayList<FeatureInfo>();

  private boolean supportParallelExecution = true;

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

  public boolean isSupportParallelExecution() {
    return supportParallelExecution;
  }

  public void setSupportParallelExecution(boolean supportParallelExecution) {
    this.supportParallelExecution = supportParallelExecution;
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

  public List<SpecInfo> getSpecsTopToBottom() {
    if (specsTopToBottom == null) {
      specsTopToBottom = new ArrayList<SpecInfo>();
      SpecInfo curr = getTopSpec();
      while (curr != null) {
        specsTopToBottom.add(curr);
        curr = curr.getSubSpec();
      }
    }

    return specsTopToBottom;
  }

  public List<SpecInfo> getSpecsBottomToTop() {
    if (specsBottomToTop == null) {
      specsBottomToTop = new ArrayList<SpecInfo>();
      SpecInfo curr = getBottomSpec();
      while (curr != null) {
        specsBottomToTop.add(curr);
        curr = curr.getSuperSpec();
      }
    }
    
    return specsBottomToTop;
  }

  public MethodInfo getInitializerMethod() {
    return initializerMethod;
  }

  public void setInitializerMethod(MethodInfo initializerMethod) {
    this.initializerMethod = initializerMethod;
  }

  public MethodInfo getSharedInitializerMethod() {
    return sharedInitializerMethod;
  }

  public void setSharedInitializerMethod(MethodInfo sharedInitializerMethod) {
    this.sharedInitializerMethod = sharedInitializerMethod;
  }

  public List<MethodInfo> getSetupMethods() {
    return setupMethods;
  }

  public void addSetupMethod(MethodInfo setupMethod) {
    setupMethods.add(setupMethod);
  }

  public List<MethodInfo>  getCleanupMethods() {
    return cleanupMethods;
  }

  public void addCleanupMethod(MethodInfo cleanupMethod) {
    cleanupMethods.add(cleanupMethod);
  }

  public List<MethodInfo> getSetupSpecMethods() {
    return setupSpecMethods;
  }

  public void addSetupSpecMethod(MethodInfo setupSpecMethod) {
    setupSpecMethods.add(setupSpecMethod);
  }

  public List<MethodInfo> getCleanupSpecMethods() {
    return cleanupSpecMethods;
  }

  public void addCleanupSpecMethod(MethodInfo cleanupSpecMethod) {
    cleanupSpecMethods.add(cleanupSpecMethod);
  }

  @SuppressWarnings("unchecked")
  public Iterable<MethodInfo> getFixtureMethods() {
    return CollectionUtil.concat(setupSpecMethods, setupMethods, cleanupMethods, cleanupSpecMethods);
  }

  @SuppressWarnings("unchecked")
  public Iterable<MethodInfo> getAllFixtureMethods() {
    if (superSpec == null) return getFixtureMethods();

    return CollectionUtil.concat(superSpec.getAllFixtureMethods(), getFixtureMethods());
  }

  public List<FieldInfo> getFields() {
    return fields;
  }

  public List<FieldInfo> getAllFields() {
    if (superSpec == null) return fields;

    List<FieldInfo> result = new ArrayList<FieldInfo>(superSpec.getAllFields());
    result.addAll(fields);
    return result;
  }

  public void addField(FieldInfo field) {
    fields.add(field);
  }

  public List<FeatureInfo> getFeatures() {
    return features;
  }

  public List<FeatureInfo> getAllFeatures() {
    if (superSpec == null) return features;
    
    List<FeatureInfo> result = new ArrayList<FeatureInfo>(superSpec.getAllFeatures());
    result.addAll(features);
    return result;
  }

  public List<FeatureInfo> getAllFeaturesInExecutionOrder() {
    List<FeatureInfo> result = getAllFeatures();
    Collections.sort(result, new Comparator<FeatureInfo>() {
      public int compare(FeatureInfo f1, FeatureInfo f2) {
        return f1.getExecutionOrder() - f2.getExecutionOrder();
      }
    });
    return result;
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

  public void filterFeatures(final IFeatureFilter filter) {
    for (FeatureInfo feature: getAllFeatures()) {
      if (!filter.matches(feature))
        feature.setExcluded(true);
    }
  }

  public void sortFeatures(final IFeatureSortOrder order) {
    List<FeatureInfo> features = getAllFeatures();
    Collections.sort(features, order);
    for (int i = 0; i < features.size(); i++) 
      features.get(i).setExecutionOrder(i);
  }

  public boolean isInitializerOrFixtureMethod(String className, String methodName) {
    if (!InternalIdentifiers.INITIALIZER_AND_FIXTURE_METHODS.contains(methodName))
      return false;

    for (SpecInfo spec : getSpecsBottomToTop())
      if (spec.getReflection().getName().equals(className))
        return true;

    return false;
  }

  public String toFeatureName(String methodName) {
    for (FeatureInfo feature : getAllFeatures())
      if (feature.hasBytecodeName(methodName))
        return feature.getName();
    return methodName;
  }
}
