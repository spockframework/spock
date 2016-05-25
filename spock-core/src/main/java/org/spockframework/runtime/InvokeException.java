package org.spockframework.runtime;

import org.spockframework.runtime.model.ErrorInfo;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.IterationInfo;

class InvokeException extends Exception{
  private final FeatureInfo currentFeature;
  private final IterationInfo currentIteration;
  private final ErrorInfo errorInfo;

  public InvokeException(FeatureInfo currentFeature, IterationInfo currentIteration, ErrorInfo errorInfo) {
    this.currentFeature = currentFeature;
    this.currentIteration = currentIteration;
    this.errorInfo = errorInfo;
  }

  public FeatureInfo getCurrentFeature() {
    return currentFeature;
  }

  public IterationInfo getCurrentIteration() {
    return currentIteration;
  }

  public ErrorInfo getErrorInfo() {
    return errorInfo;
  }
}
