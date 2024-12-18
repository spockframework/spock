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
  private volatile FeatureInfo currentFeature;
  private volatile IterationInfo currentIteration;

  private volatile BlockInfo currentBlock;

  private volatile Specification sharedInstance;

  private volatile Throwable thrownException;
  private final Deque<IStoreProvider> storeProvider = new ArrayDeque<>(3); // spec, feature, iteration

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

  @Nullable
  FeatureInfo getCurrentFeatureOrNull() {
    return currentFeature;
  }

  public void setCurrentFeature(@Nullable FeatureInfo currentFeature) {
    this.currentFeature = currentFeature;
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
    return storeProvider.getLast().getStore(namespace);
  }

  public void pushStoreProvider(IStoreProvider storeProvider) {
    this.storeProvider.push(storeProvider);
  }

  public void popStoreProvider() {
    this.storeProvider.pop();
  }
}
