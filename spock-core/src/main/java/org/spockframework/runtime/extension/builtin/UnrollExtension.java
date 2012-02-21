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

import groovy.lang.Closure;
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.IterationInfo;
import org.spockframework.runtime.model.NameProvider;
import org.spockframework.runtime.model.SpecInfo;

import spock.lang.Unroll;

public class UnrollExtension extends AbstractAnnotationDrivenExtension<Unroll> {
  @Override
  public void visitSpecAnnotation(Unroll unroll, SpecInfo spec) {
    for (FeatureInfo feature : spec.getFeatures()) {
      if (feature.isParameterized()) {
        visitFeatureAnnotation(unroll, feature);
      }
    }
  }

  @Override
  public void visitFeatureAnnotation(Unroll unroll, FeatureInfo feature) {
    if (!feature.isParameterized()) return; // could also throw exception

    feature.setReportIterations(true);
    NameProvider<IterationInfo> nameProvider = chooseNameProvider(unroll, feature);
    feature.setIterationNameProvider(nameProvider);
  }

  private NameProvider<IterationInfo> chooseNameProvider(Unroll unroll, FeatureInfo feature) {
    if (unroll.value() != Closure.class) {
      return new ClosureBasedUnrollNameProvider(feature, unroll.value());
    }
    if (feature.getName().contains("#")) {
      return new UnrollNameProvider(feature, feature.getName());
    }
    return null;
  }
}
