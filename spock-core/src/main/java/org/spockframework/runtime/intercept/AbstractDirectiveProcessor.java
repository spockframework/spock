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

import java.lang.annotation.Annotation;

import org.spockframework.runtime.InvalidSpecError;
import org.spockframework.runtime.model.*;

/**
 *
 * @author Peter Niederwieser
 */
public class AbstractDirectiveProcessor<T extends Annotation> implements IDirectiveProcessor<T> {
  public void visitSpecDirective(T directive, SpecInfo spec) {
    throw new InvalidSpecError("@%s may not be applied to Specs")
        .format(directive.annotationType().getSimpleName());
  }

  public void visitFeatureDirective(T directive, FeatureInfo feature) {
    throw new InvalidSpecError("@%s may not be applied to feature methods")
        .format(directive.annotationType().getSimpleName());
  }

  public void visitFixtureDirective(T directive, MethodInfo fixtureMethod) {
    throw new InvalidSpecError("@%s may not be applied to fixture methods")
        .format(directive.annotationType().getSimpleName());
  }

  public void visitFieldDirective(T directive, FieldInfo field) {
    throw new InvalidSpecError("@%s may not be applied to fields")
        .format(directive.annotationType().getSimpleName());
  }

  public void afterVisits(SpecInfo spec) {} // do nothing
}
