/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.AbstractRunListener;
import org.spockframework.runtime.model.*;
import org.spockframework.util.NotThreadSafe;

import java.util.*;
import java.util.Map.Entry;

import groovy.lang.*;

/**
 * @author Luke Daley
 */
// TODO: implement as interceptor
@NotThreadSafe // expects beforeFeature and afterFeature to be called in matching pairs
public class RevertMetaClassRunListener extends AbstractRunListener {
  private final Map<Class<?>, MetaClass> specLevelSavedMetaClasses = new HashMap<>();
  private final Map<Class<?>, MetaClass> methodLevelSavedMetaClasses = new HashMap<>();

  private final Set<Class<?>> specRestorations;
  private final Map<String, Set<Class<?>>> methodRestorations;

  public RevertMetaClassRunListener(Set<Class<?>> specRestorations, Map<String, Set<Class<?>>> methodRestorations) {
    this.specRestorations = specRestorations;
    this.methodRestorations = methodRestorations;
  }

  @Override
  public void beforeSpec(SpecInfo spec) {
    if (specRestorations.isEmpty()) return;
    saveMetaClassesInto(specRestorations, specLevelSavedMetaClasses);
  }

  @Override
  public void beforeFeature(FeatureInfo feature) {
    if (feature.isParameterized()) return;
    if (methodRestorations.isEmpty()) return;

    String methodName = feature.getFeatureMethod().getReflection().getName();
    if (!methodRestorations.containsKey(methodName)) return;

    saveMetaClassesInto(methodRestorations.get(methodName), methodLevelSavedMetaClasses);
  }

  @Override
  public void beforeIteration(IterationInfo iteration) {
    if (!iteration.getParent().isParameterized()) return;
    if (methodRestorations.isEmpty()) return;

    String methodName = iteration.getParent().getFeatureMethod().getReflection().getName();
    if (!methodRestorations.containsKey(methodName)) return;

    saveMetaClassesInto(methodRestorations.get(methodName), methodLevelSavedMetaClasses);
  }

  @Override
  public void afterIteration(IterationInfo iteration) {
    if (!iteration.getParent().isParameterized()) return;
    if (methodLevelSavedMetaClasses.isEmpty()) return;

    revertMetaClassesFromAndClear(methodLevelSavedMetaClasses);
  }

  @Override
  public void afterFeature(FeatureInfo feature) {
    if (feature.isParameterized()) return;
    if (methodLevelSavedMetaClasses.isEmpty()) return;

    revertMetaClassesFromAndClear(methodLevelSavedMetaClasses);
  }

  @Override
  public void afterSpec(SpecInfo spec) {
    if (specRestorations.isEmpty()) return;
    revertMetaClassesFromAndClear(specLevelSavedMetaClasses);
  }

  private void saveMetaClassesInto(Set<Class<?>> toSave, Map<Class<?>, MetaClass> into) {
    MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();

    for (Class<?> clazz : toSave) {
      into.put(clazz, registry.getMetaClass(clazz));
      MetaClass newMetaClass = new ExpandoMetaClass(clazz, true, true);
      newMetaClass.initialize();
      registry.setMetaClass(clazz, newMetaClass);
    }
  }

  private void revertMetaClassesFromAndClear(Map<Class<?>, MetaClass> savedMetaClasses) {
    MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();

    for (Entry<Class<?>, MetaClass> entry : savedMetaClasses.entrySet()) {
      Class<?> clazz = entry.getKey();
      MetaClass originalMetaClass = entry.getValue();

      registry.removeMetaClass(clazz);
      registry.setMetaClass(clazz, originalMetaClass);
    }
    savedMetaClasses.clear();
  }
}
