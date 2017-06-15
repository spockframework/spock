/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.mock.runtime;

import org.spockframework.mock.*;
import org.spockframework.runtime.GroovyRuntimeUtil;
import spock.lang.Specification;

import java.lang.reflect.Modifier;
import java.util.Collections;

import groovy.lang.*;

public class GroovyMockFactory implements IMockFactory {
  public static GroovyMockFactory INSTANCE = new GroovyMockFactory();

  @Override
  public boolean canCreate(IMockConfiguration configuration) {
    return configuration.getImplementation() == MockImplementation.GROOVY;
  }

  @Override
  public Object create(IMockConfiguration configuration, Specification specification) throws CannotCreateMockException {
    final MetaClass oldMetaClass = GroovyRuntimeUtil.getMetaClass(configuration.getType());
    GroovyMockMetaClass newMetaClass = new GroovyMockMetaClass(configuration, specification, oldMetaClass);
    final Class<?> type = configuration.getType();

    if (configuration.isGlobal()) {
      if (type.isInterface()) {
        throw new CannotCreateMockException(type,
            ". Global mocking is only possible for classes, but not for interfaces.");
      }
      GroovyRuntimeUtil.setMetaClass(type, newMetaClass);
      specification.getSpecificationContext().getCurrentIteration().addCleanup(new Runnable() {
        @Override
        public void run() {
          GroovyRuntimeUtil.setMetaClass(type, oldMetaClass);
        }
      });
      return MockInstantiator.instantiate(type, type, configuration.getConstructorArgs(), configuration.isUseObjenesis());
    }

    if (isFinalClass(type)) {
      final Object instance = MockInstantiator.instantiate(type,
          type, configuration.getConstructorArgs(), configuration.isUseObjenesis());
      GroovyRuntimeUtil.setMetaClass(instance, newMetaClass);

      return instance;
    }

    IProxyBasedMockInterceptor mockInterceptor = new GroovyMockInterceptor(configuration, specification, newMetaClass);
    return ProxyBasedMockFactory.INSTANCE.create(type, Collections.<Class<?>>singletonList(GroovyObject.class),
        configuration.getConstructorArgs(), mockInterceptor, specification.getClass().getClassLoader(),
        configuration.isUseObjenesis());
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
