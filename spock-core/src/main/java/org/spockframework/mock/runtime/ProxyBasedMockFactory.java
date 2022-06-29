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
import org.spockframework.util.*;

import java.util.*;

/**
 * Some implementation details of this class and the classes it references are inspired from Spring, EasyMock
 * Class Extensions, JMock, Mockito, and this thread:
 * http://www.nabble.com/Callbacks%2C-classes-and-instances-to4092596.html
 *
 * @author Peter Niederwieser
 */
public class ProxyBasedMockFactory {
  private static final boolean ignoreByteBuddy = Boolean.getBoolean("org.spockframework.mock.ignoreByteBuddy");
  private static final boolean byteBuddyAvailable = ReflectionUtil.isClassAvailable("net.bytebuddy.ByteBuddy");
  private static final boolean cglibAvailable = ReflectionUtil.isClassAvailable("net.sf.cglib.proxy.Enhancer");

  public static ProxyBasedMockFactory INSTANCE = new ProxyBasedMockFactory();

  public Object create(Class<?> mockType, List<Class<?>> additionalInterfaces, @Nullable List<Object> constructorArgs,
      IProxyBasedMockInterceptor mockInterceptor, ClassLoader classLoader, boolean useObjenesis) throws CannotCreateMockException {
    if (mockType.isInterface()) {
      return DynamicProxyMockFactory.createMock(mockType, additionalInterfaces, constructorArgs, mockInterceptor, classLoader);
    }
    if (byteBuddyAvailable && !ignoreByteBuddy) {
      return ByteBuddyMockFactory.createMock(mockType, additionalInterfaces, constructorArgs, mockInterceptor, classLoader, useObjenesis);
    }
    if (cglibAvailable) {
      return CglibMockFactory.createMock(mockType, additionalInterfaces, constructorArgs, mockInterceptor, classLoader, useObjenesis);
    }
    throw new CannotCreateMockException(mockType,
      ". Mocking of non-interface types requires a code generation library. Please put an up-to-date version of byte-buddy or cglib-nodep on the class path."
    );
  }

}
