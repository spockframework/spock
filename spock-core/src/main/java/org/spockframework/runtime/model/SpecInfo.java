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
public class SpecInfo extends NodeInfo<NodeInfo, Class<?>> implements IMethodNameMapper {
  private final List<FieldInfo> fields = new ArrayList<FieldInfo>();
  private final List<IMethodInterceptor> interceptors = new ArrayList<IMethodInterceptor>();
  private final List<IRunListener> listeners = new ArrayList<IRunListener>();

  private String filename;
  private SpecInfo superSpec;
  private SpecInfo subSpec;
  private FieldInfo sharedInstanceField;

  private MethodInfo setupMethod;
  private MethodInfo cleanupMethod;
  private MethodInfo setupSpecMethod;
  private MethodInfo cleanupSpecMethod;
  private List<FeatureInfo> features = new ArrayList<FeatureInfo>();

  private boolean excluded = false;
  private boolean skipped = false;
  
  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
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

  public boolean isTopSpec() {
    return superSpec == null;
  }

  public SpecInfo getBottomSpec() {
    SpecInfo curr = this;
    while (curr.getSubSpec() != null)
      curr = curr.getSubSpec();
    return curr;
  }

  public boolean isBottomSpec() {
    return subSpec == null;
  }

  // TODO: may want to cache this rather than recreating it all the time
  public List<SpecInfo> getSpecsTopToBottom() {
    List<SpecInfo> specs = new ArrayList<SpecInfo>();

    SpecInfo curr = getTopSpec();
    while (curr != null) {
      specs.add(curr);
      curr = curr.getSubSpec();
    }

    return specs;
  }

  // TODO: may want to cache this rather than recreating it all the time
  public List<SpecInfo> getSpecsBottomToTop() {
    List<SpecInfo> specs = new ArrayList<SpecInfo>();

    SpecInfo curr = getBottomSpec();
    while (curr != null) {
      specs.add(curr);
      curr = curr.getSuperSpec();
    }

    return specs;
  }

  public FieldInfo getSharedInstanceField() {
    return sharedInstanceField;
  }

  public void setSharedInstanceField(FieldInfo sharedInstanceField) {
    this.sharedInstanceField = sharedInstanceField;
  }

  public MethodInfo getSetupMethod() {
    return setupMethod;
  }

  public void setSetupMethod(MethodInfo setupMethod) {
    this.setupMethod = setupMethod;
  }

  public MethodInfo getCleanupMethod() {
    return cleanupMethod;
  }

  public void setCleanupMethod(MethodInfo cleanupMethod) {
    this.cleanupMethod = cleanupMethod;
  }

  public MethodInfo getSetupSpecMethod() {
    return setupSpecMethod;
  }

  public void setSetupSpecMethod(MethodInfo setupSpecMethod) {
    this.setupSpecMethod = setupSpecMethod;
  }

  public MethodInfo getCleanupSpecMethod() {
    return cleanupSpecMethod;
  }

  public void setCleanupSpecMethod(MethodInfo cleanupSpecMethod) {
    this.cleanupSpecMethod = cleanupSpecMethod;
  }

  public List<MethodInfo> getFixtureMethods() {
    return Arrays.asList(setupSpecMethod, setupMethod, cleanupMethod, cleanupSpecMethod);
  }

  public List<MethodInfo> getAllFixtureMethods() {
    if (superSpec == null) return getFixtureMethods();

    List<MethodInfo> result = new ArrayList<MethodInfo>(superSpec.getAllFixtureMethods());
    result.addAll(getFixtureMethods());
    return result;
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

  public List<IMethodInterceptor> getInterceptors() {
    return interceptors;
  }

  public void addInterceptor(IMethodInterceptor interceptor) {
    interceptors.add(interceptor);
  }

  public List<IRunListener> getListeners() {
    return listeners;
  }

  public void addListener(IRunListener listener) {
    listeners.add(listener);
  }

  public boolean isExcluded() {
    return excluded;
  }

  public void setExcluded(boolean excluded) {
    this.excluded = excluded;
  }

  public boolean isSkipped() {
    return skipped;
  }

  public void setSkipped(boolean skipped) {
    this.skipped = skipped;
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

  public boolean isFixtureMethod(String className, String methodName) {
    if (!Identifiers.FIXTURE_METHODS.contains(methodName))
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
