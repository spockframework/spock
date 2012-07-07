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

package org.spockframework.mock;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import net.sf.cglib.proxy.*;
import org.spockframework.util.ReflectionUtil;
import spock.lang.Specification;

/**
 * Some implementation details of this class are inspired from Spring, EasyMock
 * Class Extensions, JMock, Mockito, and this thread:
 * http://www.nabble.com/Callbacks%2C-classes-and-instances-to4092596.html
 *
 * @author Peter Niederwieser
 */
public class ProxyBasedMockFactory {
  private static final boolean cglibAvailable = ReflectionUtil.isClassAvailable("net.sf.cglib.proxy.Enhancer");

  public static ProxyBasedMockFactory INSTANCE = new ProxyBasedMockFactory();

  public Object create(Class<?> mockType, List<Class<?>> additionalInterfaces,
      IProxyBasedMockInterceptor mockInterceptor, Specification spec) throws CannotCreateMockException {
    Object proxy;

    if (mockType.isInterface()) {
      proxy = createDynamicProxyMock(mockType, additionalInterfaces, mockInterceptor, spec);
    } else if (cglibAvailable) {
      proxy = CglibMockFactory.createMock(mockType, additionalInterfaces, mockInterceptor, spec);
    } else {
      throw new CannotCreateMockException(mockType,
          ". Mocking of non-interface types requires CGLIB. "
              + "To solve this problem, put cglib-nodep-2.2 or higher on the class path."
      );
    }

    return proxy;
  }

  private Object createDynamicProxyMock(Class<?> mockType, List<Class<?>> additionalInterfaces,
      IProxyBasedMockInterceptor mockInterceptor, Specification spec) {
    List<Class<?>> interfaces = new ArrayList<Class<?>>();
    interfaces.add(mockType);
    interfaces.addAll(additionalInterfaces);
    interfaces.add(IMockObjectProvider.class);
    return Proxy.newProxyInstance(
        spec.getClass().getClassLoader(),
        interfaces.toArray(new Class<?>[interfaces.size()]),
        new DynamicProxyMockInterceptorAdapter(mockInterceptor)
    );
  }

  // inner class to defer class loading
  private static class CglibMockFactory {
    static Object createMock(Class<?> type, List<Class<?>> additionalInterfaces,
        IProxyBasedMockInterceptor interceptor, Specification spec) {
      Enhancer enhancer = new ConstructorFriendlyEnhancer();
      enhancer.setClassLoader(spec.getClass().getClassLoader());
      enhancer.setSuperclass(type);
      List<Class<?>> interfaces = new ArrayList<Class<?>>();
      interfaces.addAll(additionalInterfaces);
      interfaces.add(IMockObjectProvider.class);
      enhancer.setInterfaces(interfaces.toArray(new Class<?>[interfaces.size()]));
      enhancer.setCallbackFilter(BridgeMethodAwareCallbackFilter.INSTANCE);
      MethodInterceptor cglibInterceptor = new CglibMockInterceptorAdapter(interceptor);
      enhancer.setCallbackTypes(new Class[] {cglibInterceptor.getClass(), NoOp.class});

      Class<?> enhancedType = enhancer.createClass();
      Object proxy = MockInstantiator.instantiate(type, enhancedType);
      ((Factory) proxy).setCallbacks(new Callback[] {cglibInterceptor, NoOp.INSTANCE});
      return proxy;
    }

    static class ConstructorFriendlyEnhancer extends Enhancer {
      @Override
      protected void filterConstructors(Class clazz, List constructors) {} // implement all ctors found in superclass
    }

    static class BridgeMethodAwareCallbackFilter implements CallbackFilter {
      // important to use same instance every time; otherwise, CGLIB will
      // keep creating new classes rather than reusing previously generated ones
      static BridgeMethodAwareCallbackFilter INSTANCE = new BridgeMethodAwareCallbackFilter();

      public int accept(Method method) {
        return method.isBridge() ? 1 : 0;
      }
    }
  }
}


