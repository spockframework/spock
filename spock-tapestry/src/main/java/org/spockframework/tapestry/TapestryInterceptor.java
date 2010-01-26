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

package org.spockframework.tapestry;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.SubModule;

import org.spockframework.runtime.extension.*;
import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.runtime.model.SpecInfo;

import spock.lang.Shared;

/**
 * Controls startup and shutdown of the Tapestry container,
 * and injects Tapestry-provided objects into specifications.
 *
 * @author Peter Niederwieser
 */
public class TapestryInterceptor extends AbstractMethodInterceptor {
  private final SpecInfo spec;
  private Registry registry;
  private IPerIterationManager perIterationManager;

  public TapestryInterceptor(SpecInfo spec) {
    this.spec = spec;
  }

  @Override
  public void interceptSetupSpecMethod(IMethodInvocation invocation) throws Throwable {
    startupRegistry();
    injectServices(invocation.getTarget(), true);
    invocation.proceed();
  }

  @Override
  public void interceptCleanupSpecMethod(IMethodInvocation invocation) throws Throwable {
    try {
      invocation.proceed();
    } finally {
      shutdownRegistry();
    }
  }

  @Override
  public void interceptSetupMethod(IMethodInvocation invocation) throws Throwable {
    injectServices(invocation.getTarget(), false);
    invocation.proceed();
  }

  @Override
  public void interceptCleanupMethod(IMethodInvocation invocation) throws Throwable {
    try {
      invocation.proceed();
    } finally {
      perIterationManager.cleanup();
    }
  }

  private void startupRegistry() {
    RegistryBuilder builder = new RegistryBuilder();
    builder.add(ExtensionModule.class);
    for (Class<?> module : getSubModules(spec)) builder.add(module);
    registry = builder.build();
    registry.performRegistryStartup();
    perIterationManager = registry.getService(IPerIterationManager.class);
  }

  private Class[] getSubModules(SpecInfo spec) {
    SubModule modules = spec.getReflection().getAnnotation(SubModule.class);
    return modules == null ? new Class[0] : modules.value();
  }

  private void injectServices(Object target, boolean sharedFields) throws IllegalAccessException {
    for (final FieldInfo field : spec.getFields())
      if (field.getReflection().isAnnotationPresent(Inject.class)
          && field.getReflection().isAnnotationPresent(Shared.class) == sharedFields) {
        Field rawField = field.getReflection();
        Object value = registry.getObject(rawField.getType(), createAnnotationProvider(field));
        rawField.setAccessible(true);
        rawField.set(target, value);
      }
  }

  private AnnotationProvider createAnnotationProvider(final FieldInfo field) {
    return new AnnotationProvider() {
      public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return field.getReflection().getAnnotation(annotationClass);
      }
    };
  }

  private void shutdownRegistry() {
    if (registry != null) registry.shutdown();
  }
}
