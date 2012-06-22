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
import org.spockframework.util.*;

import net.sf.cglib.proxy.*;

import groovy.lang.*;


/**
 *
 * Some implementation details of this class are stolen from Spring, EasyMock
 * Class Extensions, and this thread:
 * http://www.nabble.com/Callbacks%2C-classes-and-instances-to4092596.html
 *
 * @author Peter Niederwieser
 */
public class DefaultMockFactory implements IMockFactory {
  public static final String INSTANCE_FIELD = "INSTANCE";
  public static final DefaultMockFactory INSTANCE = new DefaultMockFactory();

  private static final boolean cglibAvailable = ReflectionUtil.isClassAvailable("net.sf.cglib.proxy.Enhancer");
  private static final boolean objenesisAvailable = ReflectionUtil.isClassAvailable("org.objenesis.Objenesis");

  public Object create(String mockName, Class<?> mockType, IInvocationDispatcher dispatcher) {
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

  private Object createDynamicProxyMock(final String mockName,
      final Class<?> mockType, final IInvocationDispatcher dispatcher) {
    return Proxy.newProxyInstance(
        mockType.getClassLoader(),
        new Class<?>[] {mockType},
        new InvocationHandler() {
          public Object invoke(Object mockInstance, Method method, Object[] args) {
            IMockObject mockObject = new MockObject(mockName, mockType, mockInstance);
            IMockMethod mockMethod = new StaticMockMethod(method);
            IMockInvocation invocation = new MockInvocation(mockObject, mockMethod, normalizeArgs(args));
            return dispatcher.dispatch(invocation);
          }
        }
    );
  }

  private static List<Object> normalizeArgs(Object[] args) {
    return args == null ? Collections.emptyList() : Arrays.asList(args);
  }

  private static class CglibMockFactory {
    static Object create(final String mockName, final Class<?> mockType, final IInvocationDispatcher dispatcher) {
      Enhancer enhancer = new Enhancer();
      enhancer.setSuperclass(mockType);
      final boolean isGroovyObject = GroovyObject.class.isAssignableFrom(mockType);

      MethodInterceptor interceptor = new MethodInterceptor() {
        public Object intercept(Object mockInstance, Method method, Object[] args, MethodProxy proxy) {
          if (isGroovyObject) {
            if (isMethod(method, "getMetaClass")) {
              return GroovyRuntimeUtil.getMetaClass(mockInstance.getClass());
            }
            if (isMethod(method, "setProperty", String.class, Object.class)) {
              Throwable throwable = new Throwable();
              StackTraceElement mockCaller = throwable.getStackTrace()[2];
              if (mockCaller.getClassName().equals("org.codehaus.groovy.runtime.ScriptBytecodeAdapter")) {
                // for some reason, runtime dispatches direct property access on mock classes via ScriptBytecodeAdapter
                // delegate to the corresponding setter method
                String methodName = GroovyRuntimeUtil.propertyToMethodName("set", (String) args[0]);
                return GroovyRuntimeUtil.invokeMethod(mockInstance, methodName, args[1]);
              }
            }
          }
          IMockObject mockObject = new MockObject(mockName, mockType, mockInstance);
          IMockMethod mockMethod = new StaticMockMethod(method);
          IMockInvocation invocation = new MockInvocation(mockObject, mockMethod, normalizeArgs(args));
          return dispatcher.dispatch(invocation);
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
"the latter has no parameterless constructor; to allow mocking of classes without parameterless constructor, put objenesis-1.2 or higher on the classpath."
        );
      }
    }

    private static boolean isMethod(Method method, String name, Class<?>... parameterTypes) {
      return method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), parameterTypes);
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


