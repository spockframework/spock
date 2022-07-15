/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.guice;

import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.model.SpecInfo;
import spock.guice.UseModules;

import java.util.*;

import com.google.inject.Module;

public class GuiceExtension implements IAnnotationDrivenExtension<UseModules> {
  private final Set<Class<? extends Module>> moduleClasses = new LinkedHashSet<>();

  @Override
  public void visitSpecAnnotations(List<UseModules> useModules, SpecInfo spec) {
    useModules
      .stream()
      .map(UseModules::value)
      .flatMap(Arrays::stream)
      .forEach(moduleClasses::add);
  }

  @Override
  public void visitSpec(SpecInfo spec) {
    if (moduleClasses.isEmpty()) return;

    GuiceInterceptor interceptor = new GuiceInterceptor(spec, moduleClasses);
    spec.addSharedInitializerInterceptor(interceptor);
    spec.addInitializerInterceptor(interceptor);
  }
}
