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

package org.spockframework.mock;

import java.lang.reflect.*;
import java.util.Collections;

import groovy.lang.*;

import org.spockframework.runtime.GroovyRuntimeUtil;

import spock.lang.Specification;

public class JavaMockFactory implements IMockFactory {
  public static JavaMockFactory INSTANCE = new JavaMockFactory();

  public boolean canCreate(MockConfiguration configuration) {
    return configuration.getImplementation() == MockImplementation.JAVA;
  }

  public Object create(MockConfiguration configuration, Specification specification) {
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
        configuration.getConstructorArgs(), interceptor, specification.getClass().getClassLoader(),
        configuration.isUseObjenesis());
  }
}


