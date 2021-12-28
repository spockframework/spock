package org.spockframework.mock.runtime;

import org.spockframework.mock.ISpockMockObject;
import org.spockframework.mock.runtime.DynamicProxyMockInterceptorAdapter;
import org.spockframework.mock.runtime.IProxyBasedMockInterceptor;
import org.spockframework.runtime.InvalidSpecException;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

class DynamicProxyMockFactory {

  private static final Class<?>[] CLASSES = new Class<?>[0];

  static Object createMock(Class<?> mockType, List<Class<?>> additionalInterfaces,
                           List<Object> constructorArgs, IProxyBasedMockInterceptor mockInterceptor, ClassLoader classLoader) {
    if (constructorArgs != null) {
      throw new InvalidSpecException("Interface based mocks may not have constructor arguments");
    }
    List<Class<?>> interfaces = new ArrayList<>();
    interfaces.add(mockType);
    interfaces.addAll(additionalInterfaces);
    interfaces.add(ISpockMockObject.class);
    return Proxy.newProxyInstance(
        classLoader,
        interfaces.toArray(CLASSES),
        new DynamicProxyMockInterceptorAdapter(mockInterceptor)
    );
  }

}
