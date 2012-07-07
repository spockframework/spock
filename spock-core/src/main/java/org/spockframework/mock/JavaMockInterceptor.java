package org.spockframework.mock;

import java.lang.reflect.Method;
import java.util.Arrays;

import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;

import org.spockframework.runtime.GroovyRuntimeUtil;
import spock.lang.Specification;
import spock.mock.MockConfiguration;
import spock.mock.MockConfiguration;
import spock.mock.MockConfiguration;
import spock.mock.MockConfiguration;

public class JavaMockInterceptor implements IProxyBasedMockInterceptor {
  private final MockConfiguration mockConfiguration;
  private final Specification specification;
  private final MetaClass mockMetaClass;

  public JavaMockInterceptor(MockConfiguration mockConfiguration, Specification specification, MetaClass mockMetaClass) {
    this.mockConfiguration = mockConfiguration;
    this.specification = specification;
    this.mockMetaClass = mockMetaClass;
  }

  public Object intercept(Object target, Method method, Object[] args) {
    IMockObject mockObject = new MockObject(mockConfiguration.getName(), mockConfiguration.getType(), target, false);

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

