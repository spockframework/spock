/*
 * Copyright 2010 the original author or authors.
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
import org.spockframework.runtime.model.IInterceptable;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.runtime.model.parallel.ExecutionMode;
import spock.util.mop.Use;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toSet;

public class UseExtension implements IStatelessAnnotationDrivenExtension<Use> {
  @Override
  public void visitSpecAnnotations(List<Use> annotations, SpecInfo spec) {
    addInterceptor(annotations, spec.getBottomSpec());

    // Disable parallel child execution for category tests
    spec.setChildExecutionMode(ExecutionMode.SAME_THREAD);
  }

  @Override
  public void visitFeatureAnnotations(List<Use> annotations, FeatureInfo feature) {
    addInterceptor(annotations, feature.getFeatureMethod());
  }

  @Override
  public void visitFixtureAnnotations(List<Use> annotations, MethodInfo fixtureMethod) {
    addInterceptor(annotations, fixtureMethod);
  }

  private void addInterceptor(List<Use> annotations, IInterceptable interceptable) {
    interceptable.addInterceptor(new UseInterceptor(
      annotations
        .stream()
        .map(Use::value)
        .flatMap(Arrays::stream)
        .collect(toSet())));
  }
}
