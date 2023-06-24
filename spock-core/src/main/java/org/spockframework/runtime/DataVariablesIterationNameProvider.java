/*
 * Copyright 2020 the original author or authors.
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

package org.spockframework.runtime;

import org.spockframework.runtime.model.*;
import org.spockframework.util.RenderUtil;

import java.util.*;

import static java.lang.String.format;

public class DataVariablesIterationNameProvider implements NameProvider<IterationInfo> {
  private final boolean includeFeatureNameForIterations;
  private final boolean includeIterationIndex;

  public DataVariablesIterationNameProvider() {
    this(true, true);
  }

  public DataVariablesIterationNameProvider(boolean includeFeatureNameForIterations) {
    this(includeFeatureNameForIterations, true);
  }

  public DataVariablesIterationNameProvider(boolean includeFeatureNameForIterations, boolean includeIterationIndex) {
    this.includeFeatureNameForIterations = includeFeatureNameForIterations;
    this.includeIterationIndex = includeIterationIndex;
  }

  @Override
  public String getName(IterationInfo iteration) {
    FeatureInfo feature = iteration.getFeature();
    if (!feature.isReportIterations()) {
      return feature.getName();
    }

    StringJoiner nameJoiner = new StringJoiner(", ");
    Map<String, Object> dataVariables = iteration.getDataVariables();
    if (dataVariables != null) {
      dataVariables.forEach((name, value) -> {
        String valueString;
        try {
          valueString = RenderUtil.toStringOrDump(value);
          int maxDataValueStringLength = 1024;
          if (valueString.length() > maxDataValueStringLength) {
            valueString = valueString.substring(0, maxDataValueStringLength) + "…";
          }
        } catch (Exception e) {
          valueString = format("#Error:%s during rendering", e.getClass().getSimpleName());
        }
        nameJoiner.add(format("%s: %s", name, valueString));
      });
    }
    if (includeIterationIndex) {
      nameJoiner.add(format("#%d", iteration.getIterationIndex()));
    }
    return includeFeatureNameForIterations ? format("%s [%s]", feature.getName(), nameJoiner) : nameJoiner.toString();
  }
}
