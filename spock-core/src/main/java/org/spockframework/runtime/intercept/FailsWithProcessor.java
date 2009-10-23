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

import org.spockframework.runtime.model.*;

import spock.lang.FailsWith;

/**
 *
 * @author Peter Niederwieser
 */
public class FailsWithProcessor extends AbstractDirectiveProcessor<FailsWith> {
  public void visitSpeckDirective(FailsWith directive, SpeckInfo speck) {
    for (FeatureInfo feature : speck.getFeatures())
      if (!feature.getFeatureMethod().getReflection().isAnnotationPresent(FailsWith.class))
        feature.getFeatureMethod().addInterceptor(new FailsWithInterceptor(directive));
  }

  public void visitFeatureDirective(FailsWith directive, FeatureInfo feature) {
    feature.getFeatureMethod().addInterceptor(new FailsWithInterceptor(directive));
  }

  public void visitFixtureDirective(FailsWith directive, MethodInfo fixtureMethod) {
    fixtureMethod.addInterceptor(new FailsWithInterceptor(directive));
  }
}
