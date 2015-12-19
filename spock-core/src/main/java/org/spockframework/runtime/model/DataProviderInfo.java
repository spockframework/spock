package org.spockframework.runtime.model;

import java.lang.reflect.AnnotatedElement;
import java.util.List;

/**
 * Internal metadata about a data provider from which the runtime model is built.
 *
 * @author Peter Niederwieser
 */
public class DataProviderInfo extends NodeInfo<FeatureInfo, AnnotatedElement> {
  private List<String> dataVariables;
  private MethodInfo dataProviderMethod;
  private List<String> parameters;

  @Override
  public AnnotatedElement getReflection() {
    throw new UnsupportedOperationException("getReflection");
  }

  public List<String> getDataVariables() {
    return dataVariables;
  }

  public void setDataVariables(List<String> dataVariables) {
    this.dataVariables = dataVariables;
  }

  public MethodInfo getDataProviderMethod() {
    return dataProviderMethod;
  }

  public void setDataProviderMethod(MethodInfo dataProviderMethod) {
    this.dataProviderMethod = dataProviderMethod;
  }

  public List<String> getParameters() {
    return parameters;
  }

  public void setParameters(List<String> parameters) {
    this.parameters = parameters;
  }
}
