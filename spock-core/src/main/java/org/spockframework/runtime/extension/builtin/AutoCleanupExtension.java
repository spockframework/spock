/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.CollectionUtil;

import spock.lang.AutoCleanup;

/**
 * @author Peter Niederwieser
 */
public class AutoCleanupExtension extends AbstractAnnotationDrivenExtension<AutoCleanup> {
  private final AutoCleanupInterceptor sharedFieldInterceptor = new AutoCleanupInterceptor();
  private final AutoCleanupInterceptor instanceFieldInterceptor = new AutoCleanupInterceptor();

  @Override
  public void visitFieldAnnotation(AutoCleanup annotation, FieldInfo field) {
    if (field.isShared()) sharedFieldInterceptor.add(field);
    else instanceFieldInterceptor.add(field);
  }

  @Override
  public void visitSpec(SpecInfo spec) {
    sharedFieldInterceptor.install(CollectionUtil.getLastElement(spec.getTopSpec().getCleanupSpecMethods()));
    instanceFieldInterceptor.install(CollectionUtil.getLastElement(spec.getTopSpec().getCleanupMethods()));
  }
}
