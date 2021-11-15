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
import org.spockframework.runtime.model.*;
import spock.lang.Ignore;

/**
 * @author Peter Niederwieser
 */
// we cannot easily support @Ignore on fixture methods because
// setup() and setupSpec() perform initialization of user-defined and internal fields
public class IgnoreExtension implements IAnnotationDrivenExtension<Ignore> {

  public static final String DEFAULT_REASON = "Ignored via @Ignore";

  @Override
  public void visitSpecAnnotation(Ignore ignore, SpecInfo spec) {
    if (spec.getIsBottomSpec() || ignore.inherited()) spec.skip(ignore.value().isEmpty() ? DEFAULT_REASON : ignore.value());
  }

  @Override
  public void visitFeatureAnnotation(Ignore ignore, FeatureInfo feature) {
    feature.skip(ignore.value().isEmpty() ? DEFAULT_REASON : ignore.value());
  }
}
