package org.spockframework.mock;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class DynamicMockMethod implements IMockMethod {
  private final String methodName;
  private final List<Class<?>> parameterTypes;
  Class<?> returnType;
  private final boolean isStatic;

  public DynamicMockMethod(String methodName, int argumentCount, boolean isStatic) {
    this(methodName, Collections.<Class<?>>nCopies(argumentCount , Object.class), Object.class, isStatic);
  }

  public DynamicMockMethod(String methodName, List<Class<?>> parameterTypes, Class<?> returnType, boolean isStatic) {
    this.methodName = methodName;
    this.parameterTypes = parameterTypes;
    this.returnType = returnType;
    this.isStatic = isStatic;
  }

  public String getName() {
    return methodName;
  }

  public List<Class<?>> getParameterTypes() {
    return parameterTypes;
  }

  public Class<?> getReturnType() {
    return returnType;
  }

  public boolean isStatic() {
    return isStatic;
  }

  public Method getTargetMethod() {
    return null;
  }
}
