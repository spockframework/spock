/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.DataVariablesIterationNameProvider;
import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.extension.*;
import org.spockframework.runtime.model.*;

import spock.lang.Rollup;
import spock.lang.Unroll;

public class UnrollExtension implements IGlobalExtension {
  private final UnrollConfiguration unrollConfiguration;

  public UnrollExtension(UnrollConfiguration unrollConfiguration) {
    this.unrollConfiguration = unrollConfiguration;
  }

  @Override
  public void visitSpec(SpecInfo spec) {
    Unroll unroll = spec.getAnnotation(Unroll.class);
    boolean unrollSpec = unroll != null;
    boolean rollupSpec = spec.getAnnotation(Rollup.class) != null;

    if (unrollSpec && rollupSpec) {
      throw new InvalidSpecException("@Unroll and @Rollup must not be used on the same spec: " + spec.getName());
    }

    boolean doUnrollSpec;
    String specUnrollPattern;
    if (unrollSpec) {
      doUnrollSpec = true;
      specUnrollPattern = unroll.value();
    } else if (rollupSpec) {
      doUnrollSpec = false;
      specUnrollPattern = "";
    } else {
      doUnrollSpec = unrollConfiguration.unrollByDefault;
      specUnrollPattern = "";
    }

    spec
      .getFeatures()
      .stream()
      .filter(FeatureInfo::isParameterized)
      .forEach(feature -> visitFeature(feature, doUnrollSpec, specUnrollPattern));

    SpecInfo superSpec = spec.getSuperSpec();
    if (superSpec != null) {
      visitSpec(superSpec);
    }
  }

  private void visitFeature(FeatureInfo feature, boolean doUnrollSpec, String specUnrollPattern) {
    MethodInfo featureMethod = feature.getFeatureMethod();
    Unroll unroll = featureMethod.getAnnotation(Unroll.class);
    boolean unrollFeature = unroll != null;
    boolean rollupFeature = featureMethod.getAnnotation(Rollup.class) != null;

    if (unrollFeature && rollupFeature) {
      throw new InvalidSpecException("@Unroll and @Rollup must not be used on the same feature: " + feature.getName());
    }

    boolean doUnrollFeature;
    String featureUnrollPattern;
    if (unrollFeature) {
      doUnrollFeature = true;
      featureUnrollPattern = unroll.value();
    } else if (rollupFeature) {
      doUnrollFeature = false;
      featureUnrollPattern = "";
    } else {
      doUnrollFeature = doUnrollSpec;
      featureUnrollPattern = "";
    }

    feature.setReportIterations(doUnrollFeature);
    feature.setIterationNameProvider(doUnrollFeature
      ? chooseNameProvider(specUnrollPattern, featureUnrollPattern, feature)
      : null);
  }

  private NameProvider<IterationInfo> chooseNameProvider(String specUnrollPattern, String featureUnrollPattern,
                                                         FeatureInfo feature) {
    if (!featureUnrollPattern.isEmpty()) {
      return new UnrollIterationNameProvider(feature, featureUnrollPattern, unrollConfiguration.validateExpressions);
    }
    if (feature.getName().contains("#")) {
      return new UnrollIterationNameProvider(feature, feature.getName(), unrollConfiguration.validateExpressions);
    }
    if (!specUnrollPattern.isEmpty()) {
      return new UnrollIterationNameProvider(feature, specUnrollPattern, unrollConfiguration.validateExpressions);
    }
    if (unrollConfiguration.defaultPattern != null) {
      return new UnrollIterationNameProvider(feature, unrollConfiguration.defaultPattern, unrollConfiguration.validateExpressions);
    }
    return new DataVariablesIterationNameProvider(unrollConfiguration.includeFeatureNameForIterations);
  }
}
