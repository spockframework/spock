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

package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.extension.AbstractGlobalExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;
import spock.lang.Rollup;
import spock.lang.Unroll;

public class RollupExtension extends AbstractAnnotationDrivenExtension<Rollup> {
  @Override
  public void visitSpecAnnotation(Rollup rollup, SpecInfo spec) {
    visitSpecAnnotation(spec, true);
  }

  private void visitSpecAnnotation(SpecInfo spec, boolean direct) {
    if (direct && (spec.getAnnotation(Unroll.class) != null)) {
      throw new InvalidSpecException("@Unroll and @Rollup must not be used on the same element");
    }

    for (FeatureInfo feature : spec.getFeatures()) {
      if (feature.isParameterized()) {
        visitFeatureAnnotation(feature, false);
      }
    }
  }

  @Override
  public void visitFeatureAnnotation(Rollup rollup, FeatureInfo feature) {
    visitFeatureAnnotation(feature, true);
  }

  private void visitFeatureAnnotation(FeatureInfo feature, boolean direct) {
    if (direct && (feature.getFeatureMethod().getAnnotation(Unroll.class) != null)) {
      throw new InvalidSpecException("@Unroll and @Rollup must not be used on the same element");
    }

    if (!feature.isParameterized()) return; // could also throw exception if direct

    feature.setReportIterations(false);
    feature.setIterationNameProvider(null);
  }

  public static class AutoApply extends AbstractGlobalExtension {
    private final boolean globalRollup = Boolean.getBoolean("spock.globalRollup");

    @Override
    public void visitSpec(SpecInfo spec) {
      if (globalRollup && spec.getAnnotation(Unroll.class) == null) {
        new RollupExtension().visitSpecAnnotation(spec, false);
      }
    }
  }
}
