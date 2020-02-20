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

import java.util.regex.*;

/**
 * @author Peter Niederwieser
 */
public class UnrollIterationNameProvider implements NameProvider<IterationInfo> {
  private static final Pattern EXPRESSION_PATTERN = Pattern.compile("#([a-zA-Z_$]([\\w$.]|\\(\\))*)");

  private final boolean assertUnrollExpressions = Boolean.getBoolean("spock.assertUnrollExpressions");
  private final FeatureInfo feature;
  private final Matcher expressionMatcher;

  public UnrollIterationNameProvider(FeatureInfo feature, String namePattern) {
    this.feature = feature;
    expressionMatcher = EXPRESSION_PATTERN.matcher(namePattern);
  }

  // always returns a name
  @Override
  public String getName(IterationInfo iterationInfo) {
    return nameFor(iterationInfo.getIterationIndex(), iterationInfo.getDataValues());
  }

  String nameFor(int iterationIndex, Object... dataValues) {
    StringBuffer result = new StringBuffer();
    expressionMatcher.reset();

    while (expressionMatcher.find()) {
      String expr = expressionMatcher.group(1);
      String value = evaluateExpression(expr, iterationIndex, dataValues);
      expressionMatcher.appendReplacement(result, Matcher.quoteReplacement(value));
    }

    expressionMatcher.appendTail(result);
    return result.toString();
  }

  private String evaluateExpression(String expr, int iterationIndex, Object[] dataValues) {
    String[] exprParts = expr.split("\\.");
    String firstPart = exprParts[0];
    Object result;

    switch (firstPart) {
      case "featureName":
        result = feature.getName();
        break;

      case "iterationCount":
        //TODO: remove this case after some time, deprecated since Spock 2.0
        System.err.println("WARNING: #iterationCount in unroll pattern is deprecated, use #iterationIndex instead");

      case "iterationIndex":
        result = String.valueOf(iterationIndex);
        break;

      default:
        int index = feature.getDataVariables().indexOf(firstPart);
        if (index < 0) {
          if (assertUnrollExpressions) {
            throw new SpockAssertionError("Error in @Unroll, could not find matching variable for expression: " + expr);
          }
          return "#Error:" + expr;
        }
        result = dataValues[index];
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
      return GroovyRuntimeUtil.toString(result);
    } catch (Exception e) {
      if (assertUnrollExpressions) {
        throw new SpockAssertionError("Error in @Unroll expression: " + expr, e);
      }
      return "#Error:" + expr;
    }
  }
}
