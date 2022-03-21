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

import org.spockframework.runtime.AbstractRunListener;
import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.model.*;
import org.spockframework.runtime.model.parallel.ExecutionMode;
import spock.lang.Stepwise;

import java.util.List;

public class StepwiseExtension implements IAnnotationDrivenExtension<Stepwise> {
  @Override
  public void visitSpecAnnotation(Stepwise annotation, final SpecInfo spec) {
    sortFeaturesInDeclarationOrder(spec);
    includeFeaturesBeforeLastIncludedFeature(spec);
    skipFeaturesAfterFirstFailingFeature(spec);

    // Disable parallel child execution for @Stepwise specs
    spec.setChildExecutionMode(ExecutionMode.SAME_THREAD);
  }

  @Override
  public void visitFeatureAnnotation(Stepwise annotation, FeatureInfo feature) {
    if (!feature.isParameterized())
      throw new InvalidSpecException(String.format(
        "Cannot use @Stepwise, feature method %s.%s is not data-driven",
        feature.getSpec().getReflection().getCanonicalName(),
        feature.getDisplayName()
      ));

    // Disable parallel iteration execution for @Stepwise features
    feature.setExecutionMode(ExecutionMode.SAME_THREAD);

    // If an error occurs in this feature, skip remaining iterations
    feature.getFeatureMethod().addInterceptor(invocation -> {
      try {
        invocation.proceed();
      }
      catch (Throwable t) {
        invocation.getFeature().skip("skipping subsequent iterations after failure");
        throw t;
      }
    });
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
      @Override
      public void error(ErrorInfo error) {
        // @Stepwise only affects class that carries the annotation,
        // but not sub- and super classes
        if (!error.getMethod().getParent().equals(spec)) return;

        // mark all subsequent features as skipped
        List<FeatureInfo> features = spec.getFeatures();
        int indexOfFailedFeature = features.indexOf(error.getMethod().getFeature());
        for (int i = indexOfFailedFeature + 1; i < features.size(); i++) {
          features.get(i).skip("Skipped due to previous Error (by @Stepwise)");
        }
      }
    });
  }
}
