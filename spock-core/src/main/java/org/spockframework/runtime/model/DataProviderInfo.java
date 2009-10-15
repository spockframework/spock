package org.spockframework.runtime.model;

import java.util.List;
import java.lang.reflect.AnnotatedElement;

/**
 * Internal metadata about a data provider from which the runtime model is built.
 *
 * @author Peter Niederwieser
 */
public class DataProviderInfo extends NodeInfo<FeatureInfo, AnnotatedElement> {
  private int line;
  private int column;
  private List<String> dataVariables;
  private MethodInfo dataProviderMethod;

  public int getLine() {
    return line;
  }

  public void setLine(int line) {
    this.line = line;
  }

  public int getColumn() {
    return column;
  }

  public void setColumn(int column) {
    this.column = column;
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
}
