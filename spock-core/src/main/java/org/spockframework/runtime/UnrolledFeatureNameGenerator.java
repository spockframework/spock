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
import org.spockframework.util.GroovyRuntimeUtil;

/**
 * @author Peter Niederwieser
 */
public class UnrolledFeatureNameGenerator {
  private static final Pattern EXPRESSION_PATTERN = Pattern.compile("#([a-zA-Z_\\$][\\w\\$\\.\\(\\)]*)");

  private final FeatureInfo feature;
  private final Matcher expressionMatcher;
  private final Map<String, Integer> parameterNameToPosition = new HashMap<String, Integer>();
  private int iterationCount;

  public UnrolledFeatureNameGenerator(FeatureInfo feature, String unrollPattern) {
    this.feature = feature;

    String namePattern = chooseNamePattern(feature, unrollPattern);
    expressionMatcher = EXPRESSION_PATTERN.matcher(namePattern);

    int pos = 0;
    for (String name : feature.getParameterNames())
      parameterNameToPosition.put(name, pos++);
  }

  public String nameFor(Object[] args) {
    StringBuffer result = new StringBuffer();
    expressionMatcher.reset();

    while (expressionMatcher.find()) {
      String expr = expressionMatcher.group(1);
      String value = evaluateExpression(expr, args);
      expressionMatcher.appendReplacement(result, Matcher.quoteReplacement(value));
    }

    expressionMatcher.appendTail(result);
    iterationCount++;
    return result.toString();
  }

  private String chooseNamePattern(FeatureInfo feature, String unrollPattern) {
    if (unrollPattern.length() > 0) {
      return unrollPattern;
    }
    if (feature.getName().contains("#")) {
      return feature.getName();
    }
    // default to same naming scheme as JUnit's @Parameterized (helps with tool support)
    return "#featureName[#iterationCount]";
  }

  private String evaluateExpression(String expr, Object[] args) {
    String[] exprParts = expr.split("\\.");
    String firstPart = exprParts[0];
    Object result;
    
    if (firstPart.equals("featureName")) {
      result = feature.getName();
    } else if (firstPart.equals("iterationCount")) {
      result = String.valueOf(iterationCount);
    } else {
      Integer pos = parameterNameToPosition.get(firstPart);
      if (pos == null) return "#" + expr;
      result = args[pos];
    }

    try {
      for (int i = 1; i < exprParts.length; i++) {
        String currPart = exprParts[i];
        if (currPart.endsWith("()")) {
          result = GroovyRuntimeUtil.invokeMethod(result, currPart.substring(0, currPart.length() - 2));
        } else {
          result = GroovyRuntimeUtil.getProperty(result, currPart);
        }
      }
      return GroovyRuntimeUtil.toString(result);
    } catch (Throwable t) {
      // can't (re)throw exceptions here because IRunSupervisor isn't supposed
      // to throw exceptions; therefore, we just don't replace this expression
      return "#" + expr;
    }
  }
}
