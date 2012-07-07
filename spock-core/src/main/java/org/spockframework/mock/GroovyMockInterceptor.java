package org.spockframework.mock;

import java.lang.reflect.Method;
import java.util.Arrays;

import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;

import org.spockframework.runtime.GroovyRuntimeUtil;

import spock.lang.Specification;
import spock.mock.MockConfiguration;
import spock.mock.MockConfiguration;
import spock.mock.MockConfiguration;

public class GroovyMockInterceptor implements IProxyBasedMockInterceptor {
  private final MockConfiguration mockConfiguration;
  private final Specification specification;
  private final MetaClass mockMetaClass;

  public GroovyMockInterceptor(MockConfiguration mockConfiguration, Specification specification, MetaClass mockMetaClass) {
    this.mockConfiguration = mockConfiguration;
    this.specification = specification;
    this.mockMetaClass = mockMetaClass;
  }

  public Object intercept(Object target, Method method, Object[] args) {
    IMockObject mockObject = new MockObject(mockConfiguration.getName(),
        mockConfiguration.getType(), target, mockConfiguration.isGlobal());

    if (method.getDeclaringClass() == IMockObjectProvider.class) {
      return mockObject;
    }

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
            mockConfiguration.getType(), new Object[] {normalizedArgs[1]}, false);
      }
      if (isMethod(method, "propertyMissing", String.class)) {
        throw new MissingPropertyException((String) normalizedArgs[0], mockConfiguration.getType());
      }
    }

    IMockMethod mockMethod = new StaticMockMethod(method);
    IMockInvocation invocation = new MockInvocation(mockObject, mockMethod, Arrays.asList(normalizedArgs));
    IMockInvocationMatcher invocationMatcher = specification.getSpecificationContext().getMockInvocationMatcher();

    InvocationMatchResult result = invocationMatcher.match(invocation);
    if (result.hasReturnValue()) return result.getReturnValue();
    return DefaultStubInteractionScope.INSTANCE.match(invocation).accept(invocation);
  }

  private boolean isMethod(Method method, String name, Class<?>... parameterTypes) {
    return method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), parameterTypes);
  }
}
