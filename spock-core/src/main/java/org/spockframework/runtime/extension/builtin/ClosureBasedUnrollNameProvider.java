/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.extension.builtin;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;

import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.IterationInfo;
import org.spockframework.runtime.model.NameProvider;
import org.spockframework.util.ObjectUtil;

public class ClosureBasedUnrollNameProvider implements NameProvider<IterationInfo> {
  private final FeatureInfo feature;
  private final Class<? extends Closure> closureClass;
  private int iterationCount;

  public ClosureBasedUnrollNameProvider(FeatureInfo feature, Class<? extends Closure> nameGeneratorType) {
    this.feature = feature;
    this.closureClass = nameGeneratorType;
  }

  public String getName(IterationInfo iterationInfo) {
    return nameFor(iterationInfo.getDataValues());
  }

  String nameFor(final Object[] dataValues) {
    iterationCount++;

    Closure nameGenerator;
    try {
      try {
        nameGenerator = closureClass.getConstructor(Object.class, Object.class).newInstance(null, null);
      } catch (NoSuchMethodException e) { // workaround for GROOVY-5040
        nameGenerator = closureClass.getConstructor(Object.class, Object.class, groovy.lang.Reference.class).newInstance(null, null, null);
      }
    } catch (Exception e) {
      return "Error: " + e;
    }

    Object delegate = new GroovyObjectSupport() {
      @Override
      public Object getProperty(String property) {
        if (property.equals("featureName")) return feature.getName();
        if (property.equals("iterationCount")) return String.valueOf(iterationCount);

        int index = feature.getDataVariables().indexOf(property);
        if (index < 0) return "$Error:" + property;

        return dataValues[index];
      }
    };

    nameGenerator.setResolveStrategy(Closure.DELEGATE_ONLY);
    nameGenerator.setDelegate(delegate);

    try {
      return ObjectUtil.toString(nameGenerator.call());
    } catch (Exception e) {
      return "Error: " + e;
    }
  }
}
