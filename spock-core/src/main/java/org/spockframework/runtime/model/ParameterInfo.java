package org.spockframework.runtime.model;

import java.lang.reflect.Parameter;

public class ParameterInfo extends NodeInfo<MethodInfo, Parameter> {
  public ParameterInfo(MethodInfo parent, String parameterName, Parameter parameter) {
    setParent(parent);
    setName(parameterName);
    setReflection(parameter);
  }
}
