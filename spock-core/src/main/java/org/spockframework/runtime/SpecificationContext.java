package org.spockframework.runtime;

import org.spockframework.lang.ISpecificationContext;
import org.spockframework.mock.IMockController;
import org.spockframework.mock.IThreadAwareMockController;
import org.spockframework.mock.runtime.MockController;
import org.spockframework.runtime.model.*;
import spock.lang.Specification;

public class SpecificationContext implements ISpecificationContext {
  private volatile SpecInfo currentSpec;
  private volatile FeatureInfo currentFeature;
  private volatile IterationInfo currentIteration;

  private volatile BlockInfo currentBlock;

  private volatile Specification sharedInstance;

  private volatile Throwable thrownException;

  private final MockController mockController = new MockController();

  public static final String GET_SHARED_INSTANCE = "getSharedInstance";
  public Specification getSharedInstance() {
    return sharedInstance;
  }

  public void setSharedInstance(Specification sharedInstance) {
    this.sharedInstance = sharedInstance;
  }

  @Override
  public SpecInfo getCurrentSpec() {
    return currentSpec;
  }

  public void setCurrentSpec(SpecInfo currentSpec) {
    this.currentSpec = currentSpec;
  }

  @Override
  public FeatureInfo getCurrentFeature() {
    if (currentFeature == null) {
      throw new IllegalStateException("Cannot request current feature in @Shared context");
    }
    return currentFeature;
  }

  public void setCurrentFeature(FeatureInfo currentFeature) {
    this.currentFeature = currentFeature;
  }

  @Override
  public IterationInfo getCurrentIteration() {
    if (isSharedContext()) {
      throw new IllegalStateException("Cannot request current iteration in @Shared context, or feature context");
    }
    return currentIteration;
  }

  void setCurrentBlock(BlockInfo blockInfo) {
    this.currentBlock = blockInfo;
  }

  @Override
  public BlockInfo getCurrentBlock() {
    return currentBlock;
  }

  public void setCurrentIteration(IterationInfo currentIteration) {
    this.currentIteration = currentIteration;
  }

  @Override
  public Throwable getThrownException() {
    return thrownException;
  }

  public static String SET_THROWN_EXCEPTION = "setThrownException";
  public void setThrownException(Throwable exception) {
    thrownException = exception;
  }

  public static String GET_MOCK_CONTROLLER = "getMockController";
  @Override
  public IMockController getMockController() {
    return mockController;
  }

  @Override
  public IThreadAwareMockController getThreadAwareMockController() {
    return mockController;
  }

  @Override
  public boolean isSharedContext() {
    return currentFeature == null || currentIteration == null;
  }
}
