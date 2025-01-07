package org.spockframework.runtime;

import org.spockframework.lang.ISpecificationContext;
import org.spockframework.mock.IMockController;
import org.spockframework.mock.IThreadAwareMockController;
import org.spockframework.mock.runtime.MockController;
import org.spockframework.runtime.extension.IStore;
import org.spockframework.runtime.extension.IStoreProvider;
import org.spockframework.runtime.model.*;
import org.spockframework.util.Nullable;
import spock.lang.Specification;

import java.util.ArrayDeque;
import java.util.Deque;

public class SpecificationContext implements ISpecificationContext {
  private volatile SpecInfo currentSpec;
  private volatile IterationInfo currentIteration;

  private volatile BlockInfo currentBlock;

  private volatile Specification sharedInstance;

  private volatile Throwable thrownException;
  private volatile IStoreProvider storeProvider; // shared spec or iteration

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
    // before an iteration is available we are in the shared context, even in the feature interceptor,
    // so the current feature cannot be set as features can run in parallel
    // so getting the current feature from the specification context would not properly work
    if (currentIteration == null) {
      throw new IllegalStateException("Cannot request current feature in @Shared context, or feature context");
    }
    return currentIteration.getFeature();
  }

  @Nullable
  FeatureInfo getCurrentFeatureOrNull() {
    return currentIteration == null ? null : currentIteration.getFeature();
  }

  @Override
  public IterationInfo getCurrentIteration() {
    if (currentIteration == null) {
      throw new IllegalStateException("Cannot request current iteration in @Shared context, or feature context");
    }
    return currentIteration;
  }

  @Nullable
  IterationInfo getCurrentIterationOrNull() {
    return currentIteration;
  }

  public void setCurrentIteration(@Nullable IterationInfo currentIteration) {
    this.currentIteration = currentIteration;
  }

  public static final String SET_CURRENT_BLOCK = "setCurrentBlock";
  public void setCurrentBlock(@Nullable BlockInfo blockInfo) {
    this.currentBlock = blockInfo;
  }

  public static final String GET_CURRENT_BLOCK = "getCurrentBlock";

  @Nullable
  @Override
  public BlockInfo getCurrentBlock() {
    return currentBlock;
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
  public IStore getStore(IStore.Namespace namespace) {
    return storeProvider.getStore(namespace);
  }

  public void setStoreProvider(IStoreProvider storeProvider) {
    this.storeProvider = storeProvider;
  }
}
