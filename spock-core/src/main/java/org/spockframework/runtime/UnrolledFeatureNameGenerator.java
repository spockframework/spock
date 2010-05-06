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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.util.NullSafe;

import spock.lang.Unroll;

/**
 * @author Peter Niederwieser
 */
public class UnrolledFeatureNameGenerator {
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("#([a-zA-Z_\\$][\\w\\$]*)");

  private final FeatureInfo feature;
  private final Matcher variableMatcher;
  private final Map<String, Integer> parameterNameToPosition = new HashMap<String, Integer>();
  private int iterationCount;

  public UnrolledFeatureNameGenerator(FeatureInfo feature, Unroll unroll) {
    this.feature = feature;
    variableMatcher = VARIABLE_PATTERN.matcher(unroll.value());

    int pos = 0;
    for (String name : feature.getParameterNames())
      parameterNameToPosition.put(name, pos++);
  }

  public String nameFor(Object[] args) {
    StringBuffer result = new StringBuffer();
    variableMatcher.reset();

    while (variableMatcher.find()) {
      String variableName = variableMatcher.group(1);
      String value = getValue(variableName, args);
      variableMatcher.appendReplacement(result, value);
    }

    variableMatcher.appendTail(result);
    iterationCount++;
    return result.toString();
  }

  private String getValue(String variableName, Object[] args) {
    if (variableName.equals("featureName")) return feature.getName();
    if (variableName.equals("iterationCount")) return String.valueOf(iterationCount);

    Integer pos = parameterNameToPosition.get(variableName);
    if (pos == null) return "#" + variableName;

    Object arg = args[pos];
    try {
      return NullSafe.toString(arg);
    } catch (Throwable t) {
      // since arg is provided by user code, we must be ready for any exception
      // to occur; rethrowing an exception would currently be interpreted as a
      // Spock bug, because IRunSupervisor isn't supposed to throw exceptions;
      // therefore, we just don't replace this variable
      return "#" + variableName;
    }
  }
}
