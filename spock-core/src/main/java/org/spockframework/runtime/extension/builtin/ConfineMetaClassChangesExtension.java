/*
 * Copyright 2010 the original author or authors.
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
import org.spockframework.runtime.model.IInterceptable;
import org.spockframework.runtime.model.SpecInfo;
import spock.util.mop.ConfineMetaClassChanges;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toSet;

/**
 * @author Luke Daley
 * @author Peter Niederwieser
 */
public class ConfineMetaClassChangesExtension implements IAnnotationDrivenExtension<ConfineMetaClassChanges> {
  @Override
  public void visitSpecAnnotations(List<ConfineMetaClassChanges> annotations, SpecInfo spec) {
    addInterceptor(annotations, spec.getBottomSpec());
  }

  @Override
  public void visitFeatureAnnotations(List<ConfineMetaClassChanges> annotations, FeatureInfo feature) {
    addInterceptor(annotations, feature.getFeatureMethod());
  }

  private void addInterceptor(List<ConfineMetaClassChanges> annotations, IInterceptable interceptable) {
    interceptable.addInterceptor(new ConfineMetaClassChangesInterceptor(
      annotations
        .stream()
        .map(ConfineMetaClassChanges::value)
        .flatMap(Arrays::stream)
        .collect(toSet())));
  }
}
