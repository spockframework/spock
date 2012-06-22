package org.spockframework.mock;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class DynamicMockMethod implements IMockMethod {
  private final String methodName;
  private List<Class<?>> parameterTypes;

  @SuppressWarnings("unchecked")
  public DynamicMockMethod(String methodName, List<Object> arguments) {
    this.methodName = methodName;
    parameterTypes = (List) Collections.nCopies(arguments.size(), Object.class);
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

  public Method getTargetMethod() {
    return null;
  }
}
