/*
 * Copyright 2020 the original author or authors.
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

package org.spockframework.runtime;

import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.IterationInfo;
import org.spockframework.runtime.model.NameProvider;

import java.util.Map;
import java.util.StringJoiner;

import static java.lang.String.format;
import static org.spockframework.util.RenderUtil.toStringOrDump;

public class DataVariablesIterationNameProvider implements NameProvider<IterationInfo> {
  @Override
  public String getName(IterationInfo iteration) {
    FeatureInfo feature = iteration.getFeature();
    if (!feature.isReportIterations()) {
      return feature.getName();
    }

    StringJoiner nameJoiner = new StringJoiner(", ", "[", "]");
    Map<String, Object> dataVariables = iteration.getDataVariables();
    if (dataVariables != null) {
      dataVariables.forEach((name, value) ->
      {
        String valueString;
        try {
          valueString = toStringOrDump(value);
        } catch (Exception e) {
          valueString = format("#Error:%s during rendering", e.getClass().getSimpleName());
        }
        nameJoiner.add(format("%s: %s", name, valueString));
      });
    }
    nameJoiner.add(format("#%d", iteration.getIterationIndex()));
    return format("%s %s", feature.getName(), nameJoiner.toString());
  }
}
