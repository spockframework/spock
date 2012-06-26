package org.spockframework.mock;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class DynamicMockMethod implements IMockMethod {
  private final String methodName;
  private final List<Class<?>> parameterTypes;
  private final boolean isStatic;

  @SuppressWarnings("unchecked")
  public DynamicMockMethod(String methodName, int parameterCount, boolean isStatic) {
    this.methodName = methodName;
    parameterTypes = (List) Collections.nCopies(parameterCount, Object.class);
    this.isStatic = isStatic;
  }

  public String getName() {
    return methodName;
  }

  public List<Class<?>> getParameterTypes() {
    return parameterTypes;
  }

  public Class<?> getReturnType() {
    return Object.class;
  }

  public boolean isStatic() {
    return isStatic;
  }

  public Method getTargetMethod() {
    return null;
  }
}
