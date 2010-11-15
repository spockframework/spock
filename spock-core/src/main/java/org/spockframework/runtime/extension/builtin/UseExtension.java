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

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.SpecInfo;

import spock.lang.Use;

public class UseExtension extends AbstractAnnotationDrivenExtension<Use> {
  public void visitSpecAnnotation(Use use, SpecInfo spec) {
    spec.getBottomSpec().addInterceptor(new UseInterceptor(use));
  }

  public void visitFeatureAnnotation(Use use, FeatureInfo feature) {
    feature.getFeatureMethod().addInterceptor(new UseInterceptor(use));
  }

  @Override
  public void visitFixtureAnnotation(Use use, MethodInfo fixtureMethod) {
    fixtureMethod.addInterceptor(new UseInterceptor(use));
  }
}