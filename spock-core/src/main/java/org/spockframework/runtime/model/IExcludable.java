package org.spockframework.runtime.model;

public interface IExcludable {
  boolean isExcluded();

  void setExcluded(boolean excluded);
}
