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

import org.spockframework.runtime.extension.*;

import java.util.*;

import groovy.lang.*;

/**
 * @author Luke Daley
 * @author Peter Niederwieser
 */
public class ConfineMetaClassChangesInterceptor implements IMethodInterceptor {
  private final Collection<Class<?>> classes;
  private final List<MetaClass> originalMetaClasses = new ArrayList<>();

  public ConfineMetaClassChangesInterceptor(Collection<Class<?>> classes) {
    this.classes = classes;
  }

  @Override
  public void intercept(IMethodInvocation invocation) throws Throwable {
    MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();

    for (Class<?> clazz : classes) {
      originalMetaClasses.add(registry.getMetaClass(clazz));
      MetaClass temporaryMetaClass = new ExpandoMetaClass(clazz, true, true);
      temporaryMetaClass.initialize();
      registry.setMetaClass(clazz, temporaryMetaClass);
    }

    try {
      invocation.proceed();
    } finally {
      for (MetaClass original : originalMetaClasses) {
        registry.setMetaClass(original.getTheClass(), original);
      }
      originalMetaClasses.clear();
    }
  }
}
