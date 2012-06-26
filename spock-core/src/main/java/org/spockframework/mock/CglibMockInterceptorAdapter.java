package org.spockframework.mock;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class CglibMockInterceptorAdapter implements MethodInterceptor {
  private final IProxyBasedMockInterceptor interceptor;

  public CglibMockInterceptorAdapter(IProxyBasedMockInterceptor interceptor) {
    this.interceptor = interceptor;
  }

  public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
    return interceptor.intercept(obj, method, args);
  }
}
