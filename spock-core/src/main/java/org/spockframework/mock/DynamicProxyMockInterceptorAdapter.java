package org.spockframework.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class DynamicProxyMockInterceptorAdapter implements InvocationHandler {
  private final IProxyBasedMockInterceptor interceptor;

  public DynamicProxyMockInterceptorAdapter(IProxyBasedMockInterceptor interceptor) {
    this.interceptor = interceptor;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    return interceptor.intercept(proxy, method, args);
  }
}
