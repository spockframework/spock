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
import org.spockframework.runtime.model.SpecInfo;

import spock.lang.Ignore;

/**
 * @author Peter Niederwieser
 */
// we cannot easily support @Ignore on fixture methods because
// setup() and setupSpec() perform initialization of user-defined and internal fields
public class IgnoreExtension extends AbstractAnnotationDrivenExtension<Ignore> {
  public void visitSpecAnnotation(Ignore ignore, SpecInfo spec) {
    spec.addInterceptor(new IgnoreInterceptor(ignore.value()));
  }

  public void visitFeatureAnnotation(Ignore ignore, FeatureInfo feature) {
    feature.addInterceptor(new IgnoreInterceptor(ignore.value()));
  }
}