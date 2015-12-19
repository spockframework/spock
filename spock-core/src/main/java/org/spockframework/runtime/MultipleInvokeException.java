package org.spockframework.runtime;

import org.spockframework.runtime.model.ErrorInfo;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.IterationInfo;

import java.util.List;

class MultipleInvokeException extends Exception{
  private final List<InvokeException> invokeExceptions;

  public MultipleInvokeException(List<InvokeException> invokeExceptions) {
    this.invokeExceptions = invokeExceptions;
  }

  public List<InvokeException> getInvokeExceptions() {
    return invokeExceptions;
  }
}
