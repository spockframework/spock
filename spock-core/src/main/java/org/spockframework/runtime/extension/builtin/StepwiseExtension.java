/*
 * Copyright 2010 the original author or authors.
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

import java.lang.annotation.Annotation;
import java.util.*;

import org.spockframework.runtime.AbstractRunListener;
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.*;

public class StepwiseExtension extends AbstractAnnotationDrivenExtension {
  public void visitSpecAnnotation(Annotation annotation, final SpecInfo spec) {
    sortFeaturesInDeclarationOrder(spec);
    includeFeaturesBeforeLastIncludedFeature(spec);
    skipFeaturesAfterFirstFailingFeature(spec);

    forceSequential(spec);
  }

  private void forceSequential(SpecInfo spec) {
    spec.setSupportParallelExecution(false);
    for (FeatureInfo featureInfo : spec.getAllFeaturesInExecutionOrder()) {
      featureInfo.setSupportParallelExecution(false);
    }
  }

  private void sortFeaturesInDeclarationOrder(SpecInfo spec) {
    for (FeatureInfo feature : spec.getFeatures())
      feature.setExecutionOrder(feature.getDeclarationOrder());
  }

  private void includeFeaturesBeforeLastIncludedFeature(SpecInfo spec) {
    List<FeatureInfo> features = spec.getFeatures();
    boolean includeRemaining = false;

    for (int i = features.size() - 1; i >= 0; i--) {
      FeatureInfo feature = features.get(i);
      if (includeRemaining) feature.setExcluded(false);
      else if (!feature.isExcluded()) includeRemaining = true;
    }
  }

  private void skipFeaturesAfterFirstFailingFeature(final SpecInfo spec) {
    spec.getBottomSpec().addListener(new AbstractRunListener() {
      public void error(ErrorInfo error) {
        // @Stepwise only affects class that carries the annotation,
        // but not sub- and super classes
        if (!error.getMethod().getParent().equals(spec)) return;

        // we can just set skip flag on all features, even though
        // some of them might already have run
        for (FeatureInfo feature : spec.getFeatures())
          feature.setSkipped(true);
      }
    });
  }
}
