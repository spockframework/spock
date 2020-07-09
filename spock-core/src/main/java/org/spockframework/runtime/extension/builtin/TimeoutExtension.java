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

package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.SpecInfo;

import spock.lang.Timeout;

/**
 * @author Peter Niederwieser
 */
public class TimeoutExtension implements IAnnotationDrivenExtension<Timeout> {
  @Override
  public void visitSpecAnnotation(Timeout timeout, SpecInfo spec) {
    for (FeatureInfo feature : spec.getFeatures()) {
      if (!feature.getFeatureMethod().getReflection().isAnnotationPresent(Timeout.class)) {
        visitFeatureAnnotation(timeout, feature);
      }
    }
  }

  @Override
  public void visitFeatureAnnotation(Timeout timeout, FeatureInfo feature) {
    feature.getFeatureMethod().addInterceptor(new TimeoutInterceptor(timeout));
  }

  @Override
  public void visitFixtureAnnotation(Timeout timeout, MethodInfo fixtureMethod) {
    fixtureMethod.addInterceptor(new TimeoutInterceptor(timeout));
  }
}
