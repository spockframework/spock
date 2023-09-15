/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.mock.runtime;

import org.spockframework.mock.*;
import org.spockframework.runtime.GroovyRuntimeUtil;
import org.spockframework.util.ReflectionUtil;
import org.spockframework.util.SpockDocLinks;
import spock.lang.Specification;

import java.lang.reflect.Modifier;
import java.util.*;

import groovy.lang.*;

public class GroovyMockFactory implements IMockFactory {
  public static final GroovyMockFactory INSTANCE = new GroovyMockFactory();

  @Override
  public boolean canCreate(IMockConfiguration configuration) {
    return configuration.getImplementation() == MockImplementation.GROOVY;
  }

  @Override
  public Object create(IMockConfiguration configuration, Specification specification) throws CannotCreateMockException {
    final Class<?> type = configuration.getType();
    final MetaClass oldMetaClass = GroovyRuntimeUtil.getMetaClass(configuration.getType());
    if (oldMetaClass instanceof GroovyMockMetaClass) {
      throw new CannotCreateMockException(type,
        ". The given type is already mocked by Spock.");
    }
    GroovyMockMetaClass newMetaClass = new GroovyMockMetaClass(configuration, specification, oldMetaClass);

    if (configuration.isGlobal()) {
      if (type.isInterface()) {
        throw new CannotCreateMockException(type,
            ". Global mocking is only possible for classes, but not for interfaces.");
      }
      if (!configuration.getAdditionalInterfaces().isEmpty()) {
        throw new CannotCreateMockException(type,
          ". Global cannot add additionalInterfaces.");
      }
      GroovyRuntimeUtil.setMetaClass(type, newMetaClass);
      specification.getSpecificationContext().getCurrentIteration().addCleanup(() -> GroovyRuntimeUtil.setMetaClass(type, oldMetaClass));
      return MockInstantiator.instantiate(type, type, configuration.getConstructorArgs(), configuration.isUseObjenesis());
    }

    if (isFinalClass(type)) {
      if (!configuration.getAdditionalInterfaces().isEmpty()) {
        throw new CannotCreateMockException(type,
          ". Cannot add additionalInterfaces to final classes.");
      }
      final Object instance = MockInstantiator.instantiate(type,
          type, configuration.getConstructorArgs(), configuration.isUseObjenesis());
      GroovyRuntimeUtil.setMetaClass(instance, newMetaClass);

      return instance;
    }

    IProxyBasedMockInterceptor mockInterceptor = new GroovyMockInterceptor(configuration, specification, newMetaClass);
    List<Class<?>> additionalInterfaces = new ArrayList<>(configuration.getAdditionalInterfaces());
    additionalInterfaces.add(GroovyObject.class);
    Object proxy = ProxyBasedMockFactory.INSTANCE.create(type, additionalInterfaces,
      configuration.getConstructorArgs(), mockInterceptor, specification.getClass().getClassLoader(),
      configuration.isUseObjenesis());
    if ((configuration.getNature() == MockNature.SPY) && (configuration.getInstance() != null)) {
      try {
        ReflectionUtil.deepCopyFields(configuration.getInstance(), proxy);
      } catch (Exception e) {
        throw new CannotCreateMockException(type,
          ". Cannot copy fields.\n" + SpockDocLinks.SPY_ON_JAVA_17.getLink(),
          e);
      }
    }
    return proxy;
  }

  private boolean isFinalClass(Class<?> type) {
    return !type.isInterface() && Modifier.isFinal(type.getModifiers());
  }

  @Override
  public Object createDetached(IMockConfiguration configuration, ClassLoader classLoader) {
    throw new CannotCreateMockException(configuration.getType(),
        ". Detached mocking is only possible for JavaMocks but not GroovyMocks at the moment.");
  }
}
