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

package org.spockframework.guice;

import java.util.*;

import com.google.inject.Module;

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.SpecInfo;

import spock.guice.UseModules;

public class GuiceExtension extends AbstractAnnotationDrivenExtension<UseModules> {
  private final Set<Class<? extends Module>> moduleClasses = new LinkedHashSet<Class<? extends Module>>();

  @Override
  public void visitSpecAnnotation(UseModules useModules, SpecInfo spec) {
    moduleClasses.addAll(Arrays.asList(useModules.value()));
  }

  @Override
  public void visitSpec(SpecInfo spec) {
    if (moduleClasses.isEmpty()) return;
    
    GuiceInterceptor interceptor = new GuiceInterceptor(spec, moduleClasses);
    spec.addSharedInitializerInterceptor(interceptor);
    spec.addInitializerInterceptor(interceptor);
  }
}
