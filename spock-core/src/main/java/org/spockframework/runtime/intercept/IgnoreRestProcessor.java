/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.intercept;

import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpeckInfo;

import spock.lang.IgnoreRest;

/**
 * Processes @IgnoreRest directives.
 *
 * @author Peter Niederwieser
 */
public class IgnoreRestProcessor extends AbstractDirectiveProcessor<IgnoreRest> {
  @Override
  public void visitFeatureDirective(IgnoreRest directive, FeatureInfo feature) {} // do nothing

  @Override
  public void afterVisits(SpeckInfo speck) {
    for (FeatureInfo feature : speck.getFeatures())
      if (!feature.getFeatureMethod().getReflection().isAnnotationPresent(IgnoreRest.class))
        feature.addInterceptor(new IgnoreInterceptor("@IgnoreRest"));
  }
}