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

import org.spockframework.runtime.*;
import org.spockframework.runtime.model.*;

import java.util.Map;
import java.util.regex.*;

import static org.spockframework.util.RenderUtil.toStringOrDump;

/**
 * @author Peter Niederwieser
 */
public class UnrollIterationNameProvider implements NameProvider<IterationInfo> {
  private static final Pattern EXPRESSION_PATTERN = Pattern.compile("#([a-zA-Z_$]([\\w$.]|\\(\\))*)");

  private final boolean expressionsAsserted;
  private final FeatureInfo feature;
  private final Matcher expressionMatcher;

  public UnrollIterationNameProvider(FeatureInfo feature, String namePattern, boolean expressionsAsserted) {
    this.feature = feature;
    this.expressionsAsserted = expressionsAsserted;
    expressionMatcher = EXPRESSION_PATTERN.matcher(namePattern);
  }

  // always returns a name
  @Override
  public String getName(IterationInfo iterationInfo) {
    return nameFor(iterationInfo.getIterationIndex(), iterationInfo.getDataVariables());
  }

  private String nameFor(int iterationIndex, Map<String, Object> dataVariables) {
    StringBuffer result = new StringBuffer();
    expressionMatcher.reset();

    while (expressionMatcher.find()) {
      String expr = expressionMatcher.group(1);
      String value = evaluateExpression(expr, iterationIndex, dataVariables);
      expressionMatcher.appendReplacement(result, Matcher.quoteReplacement(value));
    }

    expressionMatcher.appendTail(result);
    return result.toString();
  }

  private String evaluateExpression(String expr, int iterationIndex, Map<String, Object> dataVariables) {
    String[] exprParts = expr.split("\\.");
    String firstPart = exprParts[0];
    Object result;

    switch (firstPart) {
      case "featureName":
        result = feature.getName();
        break;

      case "iterationIndex":
        result = String.valueOf(iterationIndex);
        break;

      default:
        if (!dataVariables.containsKey(firstPart)) {
          if (expressionsAsserted) {
            throw new SpockAssertionError("Error in @Unroll, could not find matching variable for expression: " + expr);
          }
          return "#Error:" + expr;
        }
        result = dataVariables.get(firstPart);
        break;
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
      return toStringOrDump(result);
    } catch (Exception e) {
      if (expressionsAsserted) {
        throw new SpockAssertionError("Error in @Unroll expression: " + expr, e);
      }
      return "#Error:" + expr;
    }
  }
}
