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

import spock.lang.Ignore;

/**
 * Processes @Ignore directives.
 *
 * @author Peter Niederwieser
 */
// we cannot easily support @Ignore on fixture methods because
// setup() and setupSpeck() perform initialization of user-defined and internal fields
public class IgnoreProcessor extends AbstractDirectiveProcessor<Ignore> {
  public void visitSpeckDirective(Ignore directive, SpeckInfo speck) {
    speck.addInterceptor(new IgnoreInterceptor(directive.value()));
  }

  public void visitFeatureDirective(Ignore directive, FeatureInfo feature) {
    feature.addInterceptor(new IgnoreInterceptor(directive.value()));
  }
}