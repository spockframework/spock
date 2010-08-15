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

package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.runtime.model.FeatureInfo;
import java.util.*;

import spock.lang.RevertMetaClass;

/**
 * @author Luke Daley
 */
public class RevertMetaClassExtension extends AbstractAnnotationDrivenExtension<RevertMetaClass> {

  private final Set<Class> specRestorations = createInitialClassSet();
  private final Map<String, Set<Class>> methodRestorations = new HashMap<String, Set<Class>>();

  @Override
  public void visitSpecAnnotation(RevertMetaClass annotation, SpecInfo spec) {
    extractClassesTo(annotation, specRestorations);
  }
  
  @Override
  public void visitFeatureAnnotation(RevertMetaClass annotation, FeatureInfo feature) {
    methodRestorations.put(
      feature.getFeatureMethod().getReflection().getName(),
      extractClassesTo(annotation, createInitialClassSet())
    );
  }
  
  @Override
  public void visitSpec(SpecInfo spec) {
    spec.addListener(new RevertMetaClassRunListener(specRestorations, methodRestorations));
  }
  
  private Set<Class> extractClassesTo(RevertMetaClass annotation, Set to) {
    Class[] value = annotation.value();
    if (value.length == 0 || (value.length == 1 && value[0] == Void.class)) {
      return to;
    }
    
    for (Class clazz : value) {
      to.add(clazz);
    }
    return to;
  }
  
  static private Set createInitialClassSet() {
    return new HashSet<Class>();
  }
}