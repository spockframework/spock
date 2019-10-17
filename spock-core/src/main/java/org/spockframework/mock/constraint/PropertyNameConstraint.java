package org.spockframework.mock.constraint;

import org.spockframework.mock.*;
import org.spockframework.runtime.GroovyRuntimeUtil;

public abstract class PropertyNameConstraint implements IInvocationConstraint {
  protected String getPropertyName(IMockInvocation invocation) {
    IMockMethod method = invocation.getMethod();
    return GroovyRuntimeUtil.getterMethodToPropertyName(
        method.getName(), method.getParameterTypes(), method.getReturnType());
  }
}
