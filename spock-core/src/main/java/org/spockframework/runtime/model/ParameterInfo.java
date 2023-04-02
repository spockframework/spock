package org.spockframework.runtime.model;

import java.lang.reflect.Parameter;
import java.util.Objects;

public class ParameterInfo extends NodeInfo<MethodInfo, Parameter> {

  private int index;

  public ParameterInfo(MethodInfo parent, String parameterName, Parameter parameter, int index) {
    setParent(parent);
    setName(parameterName);
    setReflection(parameter);
    this.index = index;
  }

  /**
   * @since 2.4
   */
  public int getIndex() {
    return index;
  }

  /**
   * @since 2.4
   */
  public void setIndex(int index) {
    this.index = index;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    ParameterInfo other = (ParameterInfo) obj;
    return index == other.index
      || Objects.equals(getName(), other.getName())
      || Objects.equals(getReflection(), other.getReflection())
      || Objects.equals(getParent().getReflection(), other.getParent().getReflection());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getReflection(), getParent().getReflection(), index);
  }
}
