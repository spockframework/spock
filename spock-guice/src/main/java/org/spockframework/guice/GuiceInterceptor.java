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

import org.spockframework.mock.MockUtil;
import org.spockframework.runtime.extension.*;
import org.spockframework.runtime.model.SpecInfo;
import spock.lang.*;

import java.lang.reflect.Field;
import java.util.*;

import com.google.inject.*;
import com.google.inject.spi.InjectionPoint;

/**
 * Creates a Guice injector, and injects Guice-provided objects into specifications.
 *
 * @author Peter Niederwieser
 */
// Important implementation detail: Only the fixture methods of
// spec.getTopSpec() are intercepted (see GuiceExtension)
public class GuiceInterceptor extends AbstractMethodInterceptor {
  private static final MockUtil MOCK_UTIL = new MockUtil();
  private final Set<Class<? extends Module>> moduleClasses;
  private final Set<InjectionPoint> injectionPoints;

  private Injector injector;

  public GuiceInterceptor(SpecInfo spec, Set<Class<? extends Module>> moduleClasses) {
    this.moduleClasses = moduleClasses;
    injectionPoints = InjectionPoint.forInstanceMethodsAndFields(spec.getReflection());
  }

  @Override
  public void interceptSharedInitializerMethod(IMethodInvocation invocation) throws Throwable {
    createInjector();
    injectValues(invocation.getSharedInstance(), true, (Specification)invocation.getInstance());
    invocation.proceed();
  }

  @Override
  public void interceptInitializerMethod(IMethodInvocation invocation) throws Throwable {
    injectValues(invocation.getInstance(), false, (Specification)invocation.getInstance());
    invocation.proceed();
  }

  private void createInjector() {
    injector = Guice.createInjector(createModules());
  }

  private List<Module> createModules() {
    List<Module> modules = new ArrayList<>();
    for (Class<? extends Module> clazz : moduleClasses) {
      try {
        modules.add(clazz.newInstance());
      } catch (InstantiationException | IllegalAccessException e) {
        throw new GuiceExtensionException("Failed to instantiate module '%s'", e).withArgs(clazz.getSimpleName());
      }
    }
    return modules;
  }

  private void injectValues(Object target, boolean sharedFields, Specification specInstance) throws IllegalAccessException {
    for (InjectionPoint point : injectionPoints) {
      if (!(point.getMember() instanceof Field))
        throw new GuiceExtensionException("Method injection is not supported; use field injection instead");

      Field field = (Field)point.getMember();
      if (field.isAnnotationPresent(Shared.class) != sharedFields) continue;

      Object value = injector.getInstance(point.getDependencies().get(0).getKey());
      if (MOCK_UTIL.isMock(value)) {
        MOCK_UTIL.attachMock(value, specInstance);
      }
      field.setAccessible(true);
      field.set(target, value);
    }
  }
}
