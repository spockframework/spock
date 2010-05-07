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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.*;
import java.lang.reflect.Proxy;
import java.util.*;

import org.objenesis.*;

import net.sf.cglib.proxy.*;

import groovy.lang.*;

import org.spockframework.util.*;

/**
 *
 * Some implementation details of this class are stolen from Spring, EasyMock
 * Class Extensions, and this thread:
 * http://www.nabble.com/Callbacks%2C-classes-and-instances-to4092596.html
 * 
 * @author Peter Niederwieser
 */
// IDEA: IntelliMock ("intelligent" return values)
// IDEA: DynaMock (based on GroovyInterceptable or groovy.lang.Interceptor)
public class DefaultMockFactory implements IMockFactory {
  public static final String INSTANCE_FIELD = "INSTANCE";

  public static final DefaultMockFactory INSTANCE = new DefaultMockFactory();
  
  private static final boolean cglibAvailable = ReflectionUtil.isClassAvailable("net.sf.cglib.proxy.Enhancer");
  private static final boolean objenesisAvailable = ReflectionUtil.isClassAvailable("org.objenesis.Objenesis");

  public Object create(String mockName, Class<?> mockType, IInvocationMatcher dispatcher) {
    if (Modifier.isFinal(mockType.getModifiers()))
      throw new CannotCreateMockException(mockType, "mocking final classes is not supported.");

    if (mockType.isInterface())
      return createDynamicProxyMock(mockName, mockType, dispatcher);

    if (cglibAvailable)
      return CglibMockFactory.create(mockName, mockType, dispatcher);
    throw new CannotCreateMockException(mockType,
"by default, only mocking of interfaces is supported; to allow mocking of classes, put cglib-nodep-2.2 or higher on the classpath."
    );
  }

  private Object createDynamicProxyMock(final String mockName, Class<?> mockType, final IInvocationMatcher dispatcher) {
    return Proxy.newProxyInstance(
      mockType.getClassLoader(),
      new Class<?>[] {mockType},
      new InvocationHandler() {
        public Object invoke(Object mock, Method method, Object[] args) {
          IMockInvocation invocation = new MockInvocation(mock, mockName, method, normalizeArgs(args));
          return dispatchInvocation(dispatcher, invocation);
        }
      }
    );
  }

  private static List<Object> normalizeArgs(Object[] args) {
    return args == null ? Collections.emptyList() : Arrays.asList(args);
  }

  private static Object dispatchInvocation(IInvocationMatcher dispatcher, IMockInvocation invocation) {
    IMockInteraction interaction = dispatcher.match(invocation);
    if (interaction == null) throw new InternalSpockError(
"invocation %s wasn't matched by any interaction (not even catch-all invocation)"
    ).withArgs(invocation);
    return interaction.accept(invocation);
  }

  private static class CglibMockFactory {
    static Object create(final String mockName, Class<?> mockType, final IInvocationMatcher dispatcher) {
      Enhancer enhancer = new Enhancer();
      enhancer.setSuperclass(mockType);
      final boolean isGroovyObject = GroovyObject.class.isAssignableFrom(mockType);

      MethodInterceptor interceptor = new MethodInterceptor() {
        public Object intercept(Object mock, Method method, Object[] args, MethodProxy proxy) {
          if (isGroovyObject && method.getName().equals("getMetaClass"))
            return GroovySystem.getMetaClassRegistry().getMetaClass(mock.getClass());
          IMockInvocation invocation = new MockInvocation(mock, mockName, method, normalizeArgs(args));
          return dispatchInvocation(dispatcher, invocation);
        }
      };

      if (objenesisAvailable) {
        enhancer.setCallbackType(interceptor.getClass());
        Object instance = ObjenesisInstantiator.instantiate(enhancer.createClass());
        ((Factory)instance).setCallback(0, interceptor);
        return instance;
      }

      try {
        enhancer.setCallback(interceptor);
        return enhancer.create(); // throws what if no parameterless superclass constructor available?
      } catch (Exception e) {
        throw new CannotCreateMockException(mockType,
"the latter has no parameterless constructor; to allow mocking of classes w/o parameterless constructor, put objenesis-1.2 or higher on the classpath."
        );
      }
    }

    private static class ObjenesisInstantiator {
      static final Objenesis objenesis = new ObjenesisStd();

      static Object instantiate(Class<?> mockType) {
        try {
          return objenesis.newInstance(mockType);
        } catch (ObjenesisException e) {
          throw new CannotCreateMockException(mockType, e);
        }
      }
    }
  }
}


