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

import org.spockframework.runtime.IFeatureFilter;
import org.spockframework.runtime.IFeatureSortOrder;
import org.spockframework.runtime.IMethodNameMapper;
import org.spockframework.runtime.intercept.IMethodInterceptor;
import org.spockframework.util.IFunction;
import org.spockframework.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Runtime information about a Spock specification.
 * 
 * @author Peter Niederwieser
 */
public class SpeckInfo extends NodeInfo<NodeInfo, Class<?>> implements IMethodNameMapper {
  private final List<FieldInfo> fields = new ArrayList<FieldInfo>();
  private final List<IMethodInterceptor> interceptors = new ArrayList<IMethodInterceptor>();

  private String filename;
  private SpeckInfo superSpeck;
  private FieldInfo sharedInstanceField;

  private MethodInfo setupMethod;
  private MethodInfo cleanupMethod;
  private MethodInfo setupSpeckMethod;
  private MethodInfo cleanupSpeckMethod;
  private List<FeatureInfo> features = new ArrayList<FeatureInfo>();

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public SpeckInfo getSuperSpeck() {
    return superSpeck;
  }

  public void setSuperSpeck(SpeckInfo superSpeck) {
    this.superSpeck = superSpeck;
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

  public MethodInfo getSetupSpeckMethod() {
    return setupSpeckMethod;
  }

  public void setSetupSpeckMethod(MethodInfo setupSpeckMethod) {
    this.setupSpeckMethod = setupSpeckMethod;
  }

  public MethodInfo getCleanupSpeckMethod() {
    return cleanupSpeckMethod;
  }

  public void setCleanupSpeckMethod(MethodInfo cleanupSpeckMethod) {
    this.cleanupSpeckMethod = cleanupSpeckMethod;
  }

  public List<FieldInfo> getFields() {
    return fields;
  }

  public void addField(FieldInfo field) {
    fields.add(field);
  }

  public List<FeatureInfo> getFeatures() {
    return features;
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

  public void filterFeatures(final IFeatureFilter filter) {
    features = Util.filterMap(features,
        new IFunction<FeatureInfo,FeatureInfo>() {
          public FeatureInfo apply(FeatureInfo value) {
            return filter.matches(value) ? value : null;
          }
        });
  }

  public void sortFeatures(final IFeatureSortOrder order) {
    Collections.sort(features, order);
  }

  public String map(String bytecodeName) {
    for (FeatureInfo feature : features)
      if (feature.hasBytecodeName(bytecodeName))
        return feature.getName();
    return bytecodeName;
  }
}
