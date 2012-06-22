package org.spockframework.mock;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class StaticMockMethod implements IMockMethod {
  private final Method method;

  public StaticMockMethod(Method method) {
    this.method = method;
  }

  public String getName() {
    return method.getName();
  }

  public List<Class<?>> getParameterTypes() {
    return Arrays.asList(method.getParameterTypes());
  }

  public Class<?> getReturnType() {
    return method.getReturnType();
  }

  public Method getTargetMethod() {
    return method;
  }
}
