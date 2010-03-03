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

import org.spockframework.runtime.AbstractRunListener;
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.*;

public class ScenarioExtension extends AbstractAnnotationDrivenExtension {
  public void visitSpecAnnotation(Annotation annotation, final SpecInfo spec) {
    spec.getBottomSpec().addListener(new AbstractRunListener() {
      public void error(ErrorInfo error) {
        // @Scenario only affects class that carries the annotation,
        // but not sub- and super classes
        if (!error.getMethod().getParent().equals(spec)) return;

        // we can just set skip flag on all features, even though
        // some of time might have already run
        for (FeatureInfo feature : spec.getFeatures())
          feature.setSkipped(true);
      }
    });
  }
}
