package org.spockframework.mock;

import java.lang.reflect.Method;
import java.util.Arrays;

import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;

import org.spockframework.util.GroovyRuntimeUtil;

public class DefaultMockInterceptor implements IProxyBasedMockInterceptor {
  private final MockSpec mockSpec;
  private final MetaClass mockMetaClass;
  private final IInvocationDispatcher dispatcher;

  public DefaultMockInterceptor(MockSpec mockSpec, MetaClass mockMetaClass, IInvocationDispatcher dispatcher) {
    this.mockSpec = mockSpec;
    this.mockMetaClass = mockMetaClass;
    this.dispatcher = dispatcher;
  }

  public Object intercept(Object target, Method method, Object[] args) {
    // sometimes we see an argument wrapped in PojoWrapper
    // example is when GroovyObject.invokeMethod is invoked directly
    Object[] normalizedArgs = GroovyRuntimeUtil.asUnwrappedArgumentArray(args);

    if (target instanceof GroovyObject) {
      if (isMethod(method, "getMetaClass")) {
        return mockMetaClass;
      }
      if (isMethod(method, "setProperty", String.class, Object.class)) {
        Throwable throwable = new Throwable();
        StackTraceElement mockCaller = throwable.getStackTrace()[3];
        if (mockCaller.getClassName().equals("org.codehaus.groovy.runtime.ScriptBytecodeAdapter")) {
          // for some reason, runtime dispatches direct property access on mock classes via ScriptBytecodeAdapter
          // delegate to the corresponding setter method
          String methodName = GroovyRuntimeUtil.propertyToMethodName("set", (String) normalizedArgs[0]);
          return GroovyRuntimeUtil.invokeMethod(target, methodName, GroovyRuntimeUtil.asArgumentArray(normalizedArgs[1]));
        }
      }
    }

    IMockObject mockObject = new MockObject(mockSpec.getName(), mockSpec.getType(), target);
    IMockMethod mockMethod = new StaticMockMethod(method);
    IMockInvocation invocation = new MockInvocation(mockObject, mockMethod, Arrays.asList(normalizedArgs));
    return dispatcher.dispatch(invocation);
  }

  private boolean isMethod(Method method, String name, Class<?>... parameterTypes) {
    return method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), parameterTypes);
  }
}

