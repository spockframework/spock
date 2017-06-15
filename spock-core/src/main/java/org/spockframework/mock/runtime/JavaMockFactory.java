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

package org.spockframework.mock.runtime;

import org.spockframework.mock.*;
import org.spockframework.runtime.GroovyRuntimeUtil;
import spock.lang.Specification;

import java.lang.reflect.Modifier;
import java.util.Collections;

import groovy.lang.MetaClass;

public class JavaMockFactory implements IMockFactory {
  public static JavaMockFactory INSTANCE = new JavaMockFactory();

  @Override
  public boolean canCreate(IMockConfiguration configuration) {
    return configuration.getImplementation() == MockImplementation.JAVA;
  }

  @Override
  public Object create(IMockConfiguration configuration, Specification specification) {
    return createInternal(configuration, specification, specification.getClass().getClassLoader());
  }

	@Override
  public Object createDetached(IMockConfiguration configuration, ClassLoader classLoader) {
		 return createInternal(configuration, null, classLoader);
	}

  private Object createInternal(IMockConfiguration configuration, Specification specification, ClassLoader classLoader) {
    if (Modifier.isFinal(configuration.getType().getModifiers())) {
      throw new CannotCreateMockException(configuration.getType(),
          " because Java mocks cannot mock final classes. If the code under test is written in Groovy, use a Groovy mock.");
    }
    if (configuration.isGlobal()) {
      throw new CannotCreateMockException(configuration.getType(),
          " because Java mocks cannot mock globally. If the code under test is written in Groovy, use a Groovy mock.");
    }

    MetaClass mockMetaClass = GroovyRuntimeUtil.getMetaClass(configuration.getType());
    IProxyBasedMockInterceptor interceptor = new JavaMockInterceptor(configuration, specification, mockMetaClass);
    return ProxyBasedMockFactory.INSTANCE.create(configuration.getType(), Collections.<Class<?>>emptyList(),
        configuration.getConstructorArgs(), interceptor, classLoader,
        configuration.isUseObjenesis());
  }
}


