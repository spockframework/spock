package org.spockframework.mock;

import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import org.spockframework.util.GroovyRuntimeUtil;

import java.lang.reflect.Method;
import java.util.Arrays;

public class GroovyMockInterceptor implements IProxyBasedMockInterceptor {
  private final MockSpec mockSpec;
  private final MetaClass mockMetaClass;
  private final IInvocationDispatcher dispatcher;

  public GroovyMockInterceptor(MockSpec mockSpec, MetaClass mockMetaClass, IInvocationDispatcher dispatcher) {
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
      if (isMethod(method, "invokeMethod", String.class, Object.class)) {
        return GroovyRuntimeUtil.invokeMethod(target,
            (String) normalizedArgs[0], GroovyRuntimeUtil.asArgumentArray(normalizedArgs[1]));
      }
      if (isMethod(method, "getProperty", String.class)) {
        String methodName = GroovyRuntimeUtil.propertyToMethodName("get", (String) normalizedArgs[0]);
        return GroovyRuntimeUtil.invokeMethod(target, methodName);
      }
      if (isMethod(method, "setProperty", String.class, Object.class)) {
        String methodName = GroovyRuntimeUtil.propertyToMethodName("set", (String) normalizedArgs[0]);
        return GroovyRuntimeUtil.invokeMethod(target, methodName, normalizedArgs[1]);
      }
      if (isMethod(method, "methodMissing", String.class, Object.class)) {
        throw new MissingMethodException((String) normalizedArgs[0],
            mockSpec.getType(), new Object[] {normalizedArgs[1]}, false);
      }
      if (isMethod(method, "propertyMissing", String.class)) {
        throw new MissingPropertyException((String) normalizedArgs[0], mockSpec.getType());
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
