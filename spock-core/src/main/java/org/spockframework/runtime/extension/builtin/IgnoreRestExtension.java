/*
 * Copyright 2009 the original author or authors.
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
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;

import spock.lang.IgnoreRest;

/**
 * @author Peter Niederwieser
 */
public class IgnoreRestExtension implements IStatelessAnnotationDrivenExtension<IgnoreRest> {
  @Override
  public void visitFeatureAnnotation(IgnoreRest ignoreRest, FeatureInfo feature) {} // do nothing

  @Override
  public void visitSpec(SpecInfo spec) {
    if (spec.getSuperSpec() != null)
      visitSpec(spec.getSuperSpec());

    for (FeatureInfo feature : spec.getFeatures()) {
      if(!feature.getFeatureMethod().getReflection().isAnnotationPresent(IgnoreRest.class))
        feature.skip("Not annotated with @IgnoreRest");
    }
  }
}
