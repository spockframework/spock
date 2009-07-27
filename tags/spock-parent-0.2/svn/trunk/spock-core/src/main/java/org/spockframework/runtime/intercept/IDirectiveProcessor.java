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

import org.spockframework.runtime.model.*;

/**
 *
 * @author Peter Niederwieser
 */
public interface IDirectiveProcessor<T extends Annotation> {
  void visitSpeckDirective(T directive, SpeckInfo speck);
  void visitFeatureDirective(T directive, FeatureInfo feature);
  void visitFixtureDirective(T directive, MethodInfo fixtureMethod);
  void visitFieldDirective(T directive, FieldInfo field);
  void afterVisits(SpeckInfo speck);
}
