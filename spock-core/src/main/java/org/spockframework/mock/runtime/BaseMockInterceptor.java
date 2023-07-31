package org.spockframework.mock.runtime;

import org.spockframework.runtime.GroovyRuntimeUtil;

import java.lang.reflect.Method;
import java.util.Arrays;

import groovy.lang.*;
import org.jetbrains.annotations.Nullable;

public abstract class BaseMockInterceptor implements IProxyBasedMockInterceptor {
  @Nullable
  protected String handleGetProperty(GroovyObject target, Object[] args) {
    // Another hack It should be replaced with something more reliable: https://github.com/spockframework/spock/issues/1076
    //Groovy 3 started to call go.getProperty("x") method instead of go.getX() directly for go.x
    String methodName = null;
    Throwable throwable = new Throwable();
    StackTraceElement mockCaller = throwable.getStackTrace()[4];
    // In some strange cases the caller classname is `groovy.lang.GroovyObject$getProperty$0` so we must use starts with here
    if (!(mockCaller.getClassName().startsWith("groovy.lang.GroovyObject$getProperty") && "call".equals(mockCaller.getMethodName()))) {
      //HACK: Only explicit getter executions (go.foo and go.getFoo()) should be deeper processed.
      //go.getProperty("foo") is treated as is (to allow for its stubbing)
      String propertyName = (String)args[0];
      MetaClass metaClass = target.getMetaClass();
      //First try the isXXX before getXXX, because this is the expected behavior, if it is boolean property.
      MetaMethod booleanVariant = metaClass
        .getMetaMethod(GroovyRuntimeUtil.propertyToMethodName("is", propertyName), GroovyRuntimeUtil.EMPTY_ARGUMENTS);
      if (booleanVariant != null && booleanVariant.getReturnType() == boolean.class) {
        methodName = booleanVariant.getName();
      } else {
        methodName = GroovyRuntimeUtil.propertyToMethodName("get", propertyName);
      }
    }
    return methodName;
  }

  protected boolean isMethod(Method method, String name, Class<?>... parameterTypes) {
    return method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), parameterTypes);
  }
}
