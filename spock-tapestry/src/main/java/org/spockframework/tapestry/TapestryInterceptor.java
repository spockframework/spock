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

import org.spockframework.runtime.intercept.IMethodInterceptor;
import org.spockframework.runtime.intercept.IMethodInvocation;
import org.spockframework.runtime.model.SpeckInfo;
import org.spockframework.util.UnreachableCodeError;

import spock.lang.Shared;

public class TapestryInterceptor implements IMethodInterceptor {
  private final SpeckInfo speck;
  private Registry registry;
  private IPerIterationManager perIterationManager;

  public TapestryInterceptor(SpeckInfo speck) {
    this.speck = speck;
  }

  public void invoke(IMethodInvocation invocation) throws Throwable {
    switch(invocation.getMethod().getKind()) {
      case SETUP_SPECK:
        startupRegistry();
        injectServices(invocation.getTarget(), true);
        invocation.proceed();
        break;
      case SETUP:
        injectServices(invocation.getTarget(), false);
        invocation.proceed();
        break;
      case CLEANUP:
        try {
          invocation.proceed();
        } finally {
          perIterationManager.cleanup();
        }
        break;
      case CLEANUP_SPECK:
        try {
          invocation.proceed();
        } finally {
          shutdownRegistry();
        }
        break;
      default:
        throw new UnreachableCodeError();
    }
    
  }

  private void startupRegistry() {
    RegistryBuilder builder = new RegistryBuilder();
    builder.add(ExtensionModule.class);
    for (Class<?> module : getSubModules(speck)) builder.add(module);
    registry = builder.build();
    registry.performRegistryStartup();
    perIterationManager = registry.getService(IPerIterationManager.class);
  }

  private Class[] getSubModules(SpeckInfo speck) {
    SubModule modules = speck.getReflection().getAnnotation(SubModule.class);
    return modules == null ? new Class[0] : modules.value();
  }

  private void injectServices(Object target, boolean sharedFields) throws IllegalAccessException {
    for (final Field field : speck.getReflection().getDeclaredFields())
      if (field.isAnnotationPresent(Inject.class)
          && field.isAnnotationPresent(Shared.class) == sharedFields) {
        Object value = registry.getObject(field.getType(), createAnnotationProvider(field));
        field.setAccessible(true);
        field.set(target, value);
      }
  }

  private AnnotationProvider createAnnotationProvider(final Field field) {
    return new AnnotationProvider() {
      public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return field.getAnnotation(annotationClass);
      }
    };
  }

  private void shutdownRegistry() {
    if (registry != null) registry.shutdown();
  }
}
