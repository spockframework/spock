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

package org.spockframework.experimental;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import groovy.lang.*;

import org.spockframework.runtime.model.FeatureInfo;

import spock.lang.Unroll;

/**
 * @author Peter Niederwieser
 */
public class ClosureBasedUnrolledFeatureNameGenerator {
  private static final Pattern PLACE_HOLDER = Pattern.compile("(.?)#");

  private final FeatureInfo feature;
  private final Closure nameGenerator;
  private int consecutiveNumber;

  public ClosureBasedUnrolledFeatureNameGenerator(GroovyShell shell, FeatureInfo feature, Unroll unroll) {
    this.feature = feature;
    String nameTemplate = convertToGString(unroll.value());

    // Note: shell.evaluate is quite slow (about 0.2 seconds on my machine)
    // doing this during speck compilation would probably be much faster
    nameGenerator = (Closure)shell.evaluate("return {\"" + nameTemplate + "\"}");

    nameGenerator.setResolveStrategy(Closure.DELEGATE_ONLY);
  }

  public String nameFor(Object[] args) {
    consecutiveNumber++;
    nameGenerator.setDelegate(new NameGeneratorValues(args));
    return nameGenerator.call().toString();
  }

  private String convertToGString(String template) {
    Matcher matcher = PLACE_HOLDER.matcher(template);
    StringBuffer result = new StringBuffer();
    while (matcher.find()) {
      String group = matcher.group(1);
      boolean escaped = "\\".equals(group);
      matcher.appendReplacement(result, escaped ? "#" : group + "\\$");
    }
    matcher.appendTail(result);
    return result.toString();
  }

  private class NameGeneratorValues extends GroovyObjectSupport {
    Object[] args;

    NameGeneratorValues(Object[] args) {
      this.args = args;     
    }

    @Override
    public Object getProperty(String name) {
      if (name.equals("featureName")) return feature.getName();
      if (name.equals("iterationCount")) return consecutiveNumber;
      for (int i = 0; i < feature.getParameterNames().size(); i++)
        if (name.equals(feature.getParameterNames().get(i))) return args[i];
 
      throw new MissingPropertyException(name);
    }

    @Override
    public void setProperty(String name, Object value) {
      throw new UnsupportedOperationException("setProperty");
    }
  }
}
