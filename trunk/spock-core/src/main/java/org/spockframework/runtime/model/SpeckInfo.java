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

import org.spockframework.runtime.IMethodInfoFilter;
import org.spockframework.runtime.IMethodInfoSortOrder;
import org.spockframework.runtime.IMethodNameMapper;
import org.spockframework.util.Util;
import org.spockframework.util.IFunction;

/**
 * Runtime information about a Spock specification.
 * 
 * @author Peter Niederwieser
 */
public class SpeckInfo extends NodeInfo<NodeInfo, Class<?>> implements IMethodNameMapper {
  private final List<FieldInfo> fields = new ArrayList<FieldInfo>();
  private MethodInfo setupMethod;
  private MethodInfo cleanupMethod;
  private MethodInfo setupSpeckMethod;
  private MethodInfo cleanupSpeckMethod;
  private List<MethodInfo> featureMethods = new ArrayList<MethodInfo>();

  public List<FieldInfo> getFields() {
    return fields;
  }

  public void addField(FieldInfo field) {
    fields.add(field);
  }

  public MethodInfo getSetupMethod() {
    return setupMethod;
  }

  public MethodInfo getCleanupMethod() {
    return cleanupMethod;
  }

  public MethodInfo getSetupSpeckMethod() {
    return setupSpeckMethod;
  }

  public MethodInfo getCleanupSpeckMethod() {
    return cleanupSpeckMethod;
  }

  public List<MethodInfo> getFeatureMethods() {
    return featureMethods;
  }

  public void addMethod(MethodInfo method) {
    switch(method.getKind()) {
      case SETUP:
        setupMethod = method;
        break;
      case CLEANUP:
        cleanupMethod = method;
        break;
      case SETUP_SPECK:
        setupSpeckMethod = method;
        break;
      case CLEANUP_SPECK:
        cleanupSpeckMethod = method;
        break;
      case FEATURE:
        featureMethods.add(method);
        break;
    }
  }

  public void filterFeatureMethods(final IMethodInfoFilter filter) {
    featureMethods = Util.filterMap(featureMethods,
        new IFunction<MethodInfo,MethodInfo>() {
          public MethodInfo apply(MethodInfo value) {
            return filter.matches(value) ? value : null;
          }
        });
  }

  public void sortFeatureMethods(final IMethodInfoSortOrder order) {
    Collections.sort(featureMethods, order);
  }

  public String map(String bytecodeName) {
    for (MethodInfo method : featureMethods)
      if (method.isAssociatedWithBytecodeName(bytecodeName))
        return method.getName();
    return bytecodeName;
  }
}
