package org.spockframework.mock;

import java.lang.reflect.Method;

public interface IProxyBasedMockInterceptor {
  Object intercept(Object target, Method method, Object[] args);
}
