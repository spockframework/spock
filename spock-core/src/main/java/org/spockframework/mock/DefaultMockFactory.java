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

import groovy.lang.*;

import org.spockframework.util.*;

/**
 * Some implementation details of this class are stolen from Spring, EasyMock
 * Class Extensions, and this thread:
 * http://www.nabble.com/Callbacks%2C-classes-and-instances-to4092596.html
 *
 * @author Peter Niederwieser
 */
public class DefaultMockFactory implements IMockFactory {
  public Object create(MockSpec mockSpec, IInvocationDispatcher dispatcher) {
    if (!mockSpec.getKind().equals("Mock")) return null;

    if (Modifier.isFinal(mockSpec.getType().getModifiers())) {
      throw new CannotCreateMockException(mockSpec, "'Mock()' cannot mock final classes. If the code under test is written in Groovy, try 'GroovyMock()'.");
    }

    DefaultMockOptions options = DefaultMockOptions.parse(mockSpec.getOptions());
    MetaClass mockMetaClass = GroovyRuntimeUtil.getMetaClass(mockSpec.getType());
    IProxyBasedMockInterceptor mockInterceptor = new DefaultMockInterceptor(mockSpec, mockMetaClass, dispatcher);
    return new ProxyBasedMockFactory(mockInterceptor, options.isForceCglib()).create(mockSpec, dispatcher);
  }
}


