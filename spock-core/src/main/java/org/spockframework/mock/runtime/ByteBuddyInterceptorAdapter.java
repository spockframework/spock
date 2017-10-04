package org.spockframework.mock.runtime;

import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;

public class ByteBuddyInterceptorAdapter {

  @RuntimeType
  public static Object interceptAbstract(@FieldValue("$spock_interceptor") IProxyBasedMockInterceptor proxyBasedMockInterceptor,
                                         @This Object self,
                                         @AllArguments Object[] arguments,
                                         @Origin Method method,
                                         @StubValue Object stubValue) throws Exception {
    Object returnValue;
    if (proxyBasedMockInterceptor == null) {
      returnValue = null; // Call before interceptor was set (constructor).
    } else {
      returnValue = proxyBasedMockInterceptor.intercept(self, method, arguments, new ByteBuddyMethodInvoker(null));
    }
    return returnValue == null ? stubValue : returnValue;
  }

  @RuntimeType
  public static Object interceptNonAbstract(@FieldValue("$spock_interceptor") IProxyBasedMockInterceptor proxyBasedMockInterceptor,
                                            @Morph ByteBuddyInvoker invoker,
                                            @This Object self,
                                            @AllArguments Object[] arguments,
                                            @Origin Method method,
                                            @StubValue Object stubValue) throws Exception {
    Object returnValue;
    if (proxyBasedMockInterceptor == null) {
      returnValue = invoker.call(arguments); // Call before interceptor was set (constructor).
    } else {
      returnValue = proxyBasedMockInterceptor.intercept(self, method, arguments, new ByteBuddyMethodInvoker(invoker));
    }
    return returnValue == null ? stubValue : returnValue;
  }

  public interface InterceptorAccess {

    void $spock_set(IProxyBasedMockInterceptor proxyBasedMockInterceptor);
  }
}
