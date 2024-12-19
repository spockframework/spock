/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IStatelessAnnotationDrivenExtension;
import org.spockframework.runtime.model.*;
import spock.lang.Retry;

/**
 * @author Leonard Brünings
 * @since 1.2
 */
public class RetryExtension implements IStatelessAnnotationDrivenExtension<Retry> {
  @Override
  public void visitSpecAnnotation(Retry annotation, SpecInfo spec) {
    if (noSubSpecWithRetryAnnotation(spec.getSubSpec())) {
      for (FeatureInfo feature : spec.getBottomSpec().getAllFeatures()) {
        if (noRetryAnnotation(feature.getFeatureMethod())) {
          visitFeatureAnnotation(annotation, feature);
        }
      }
    }
  }

  private boolean noSubSpecWithRetryAnnotation(SpecInfo spec) {
    if (spec == null) {
      return true;
    }
    return noRetryAnnotation(spec) && noSubSpecWithRetryAnnotation(spec.getSubSpec());
  }

  private boolean noRetryAnnotation(NodeInfo node) {
    return !node.getReflection().isAnnotationPresent(Retry.class);
  }

  @Override
  public void visitFeatureAnnotation(Retry annotation, FeatureInfo feature) {
    if (annotation.mode() == Retry.Mode.SETUP_FEATURE_CLEANUP) {
      feature.addIterationInterceptor(new RetryIterationInterceptor(annotation, feature.getFeatureMethod()));
    } else {
      feature.getFeatureMethod().addInterceptor(new RetryFeatureInterceptor(annotation));
    }
  }
}
