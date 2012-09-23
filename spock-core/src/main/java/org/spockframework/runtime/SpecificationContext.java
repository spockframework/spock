package org.spockframework.runtime;

import org.spockframework.mock.IMockController;
import org.spockframework.mock.MockController;
import org.spockframework.runtime.model.IterationInfo;

import org.spockframework.lang.ISpecificationContext;
import spock.lang.Specification;

public class SpecificationContext implements ISpecificationContext {
  private volatile Specification sharedInstance;

  private volatile IterationInfo iterationInfo;

  private volatile Throwable thrownException;

  private final IMockController mockController = new MockController();

  public static final String GET_SHARED_INSTANCE = "getSharedInstance";
  public Specification getSharedInstance() {
    return sharedInstance;
  }

  public void setSharedInstance(Specification sharedInstance) {
    this.sharedInstance = sharedInstance;
  }

  public IterationInfo getIterationInfo() {
    return iterationInfo;
  }

  public void setIterationInfo(IterationInfo iterationInfo) {
    this.iterationInfo = iterationInfo;
  }

  public Throwable getThrownException() {
    return thrownException;
  }

  public static String SET_THROWN_EXCEPTION = "setThrownException";
  public void setThrownException(Throwable exception) {
    thrownException = exception;
  }

  public static String GET_MOCK_CONTROLLER = "getMockController";
  public IMockController getMockController() {
    return mockController;
  }
}
