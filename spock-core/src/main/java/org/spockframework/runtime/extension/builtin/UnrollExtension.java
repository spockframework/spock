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

import org.spockframework.runtime.DataVariablesIterationNameProvider;
import org.spockframework.runtime.GroovyRuntimeUtil;
import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.extension.AbstractGlobalExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.IterationInfo;
import org.spockframework.runtime.model.NameProvider;
import org.spockframework.runtime.model.SpecInfo;

import spock.lang.Rollup;
import spock.lang.Unroll;

public class UnrollExtension extends AbstractAnnotationDrivenExtension<Unroll> {
  private final String globalUnrollPattern;

  public UnrollExtension() {
    String globalUnrollPattern = System.getProperty("spock.globalUnrollPattern");
    this.globalUnrollPattern = GroovyRuntimeUtil.isTruthy(globalUnrollPattern) ? globalUnrollPattern : null;
  }

  @Override
  public void visitSpecAnnotation(Unroll unroll, SpecInfo spec) {
    visitSpecAnnotation(unroll, spec, true);
  }

  private void visitSpecAnnotation(Unroll unroll, SpecInfo spec, boolean direct) {
    if (direct && (spec.getAnnotation(Rollup.class) != null)) {
      throw new InvalidSpecException("@Unroll and @Rollup must not be used on the same element");
    }

    for (FeatureInfo feature : spec.getFeatures()) {
      if (feature.isParameterized()) {
        visitFeatureAnnotation(unroll, feature, false);
      }
    }
  }

  @Override
  public void visitFeatureAnnotation(Unroll unroll, FeatureInfo feature) {
    visitFeatureAnnotation(unroll, feature, true);
  }

  private void visitFeatureAnnotation(Unroll unroll, FeatureInfo feature, boolean direct) {
    if (direct && (feature.getFeatureMethod().getAnnotation(Rollup.class) != null)) {
      throw new InvalidSpecException("@Unroll and @Rollup must not be used on the same element");
    }

    if (!feature.isParameterized()) return; // could also throw exception if direct

    feature.setReportIterations(true);
    feature.setIterationNameProvider(chooseNameProvider(unroll, feature));
  }

  private NameProvider<IterationInfo> chooseNameProvider(Unroll unroll, FeatureInfo feature) {
    if (unroll.value().length() > 0) {
      return new UnrollIterationNameProvider(feature, unroll.value());
    }
    if (feature.getName().contains("#")) {
      return new UnrollIterationNameProvider(feature, feature.getName());
    }
    if (globalUnrollPattern != null) {
      return new UnrollIterationNameProvider(feature, globalUnrollPattern);
    }
    return new DataVariablesIterationNameProvider();
  }

  public static class AutoApply extends AbstractGlobalExtension {
    private final boolean globalRollup = Boolean.getBoolean("spock.globalRollup");
    private final Unroll unroll = Unrolled.class.getAnnotation(Unroll.class);

    @Override
    public void visitSpec(SpecInfo spec) {
      if (!globalRollup && spec.getAnnotation(Rollup.class) == null) {
        new UnrollExtension().visitSpecAnnotation(unroll, spec, false);
      }
    }

    @Unroll
    private static final class Unrolled {
    }
  }
}
