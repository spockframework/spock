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

package org.spockframework.runtime;

import java.util.HashMap;
import java.util.Map;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingPropertyException;

import org.spockframework.runtime.extension.ExtensionException;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.util.NullSafe;

/**
 * @author Peter Niederwieser
 */
public class UnrolledFeatureNameGenerator {
  private final FeatureInfo feature;
  private final Class<? extends Closure> nameGeneratorClass;
  private final Map<String, Integer> parameterNameToPosition = new HashMap<String, Integer>();

  private int iterationCount = -1;

  public UnrolledFeatureNameGenerator(FeatureInfo feature, Class<? extends Closure> nameGeneratorClass) {
    this.feature = feature;
    this.nameGeneratorClass = nameGeneratorClass;

    int pos = 0;
    for (String name : feature.getParameterNames())
      parameterNameToPosition.put(name, pos++);
  }

  public String nameFor(final Object[] args) {
    iterationCount++;

    if (nameGeneratorClass == Closure.class)
      return String.format("%s[%d]", feature.getName(), iterationCount);

    Closure nameGenerator;
    try {
      try {
        nameGenerator = nameGeneratorClass.getConstructor(Object.class, Object.class).newInstance(null, null);
      } catch (NoSuchMethodException e) { // workaround for GROOVY-5040
        nameGenerator = nameGeneratorClass.getConstructor(Object.class, Object.class, groovy.lang.Reference.class).newInstance(null, null, null);
      }
    } catch (Exception e) {
      throw new ExtensionException("Failed to instantiate @Unroll naming pattern", e);
    }

    Object delegate = new GroovyObjectSupport() {
      @Override
      public Object getProperty(String property) {
        if (property.equals("featureName")) return feature.getName();
        if (property.equals("iterationCount")) return String.valueOf(iterationCount);

        Integer pos = parameterNameToPosition.get(property);
        if (pos == null) throw new MissingPropertyException(
            String.format("Cannot resolve data variable '%s'", property));

        return args[pos];
      }
    };

    nameGenerator.setResolveStrategy(Closure.DELEGATE_ONLY);
    nameGenerator.setDelegate(delegate);

    String name;
    try {
      name = NullSafe.toString(nameGenerator.call());
    } catch (Exception e) {
      throw new ExtensionException("Failed to evaluate @Unroll naming pattern", e);
    }

    return name;
  }
}
