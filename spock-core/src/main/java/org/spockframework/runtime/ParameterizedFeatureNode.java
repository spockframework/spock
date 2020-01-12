package org.spockframework.runtime;

import org.spockframework.runtime.model.FeatureInfo;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.platform.engine.*;

public class ParameterizedFeatureNode extends FeatureNode {

  protected ParameterizedFeatureNode(UniqueId uniqueId, FeatureInfo featureInfo) {
    super(uniqueId, featureInfo.getName(),featureToMethodSource(featureInfo), featureInfo);
  }


  @Override
  public SpockExecutionContext prepare(SpockExecutionContext context) throws Exception {
    featureInfo.setIterationNameProvider(new SafeIterationNameProvider(featureInfo.getIterationNameProvider()));
    return context.withCurrentFeature(featureInfo).withParentId(getUniqueId());
  }

  @Override
  public SpockExecutionContext execute(SpockExecutionContext context, DynamicTestExecutor dynamicTestExecutor) throws Exception {
    verifyNotSkipped(featureInfo);
    ErrorInfoCollector errorInfoCollector = new ErrorInfoCollector();
    context = context.withErrorInfoCollector(errorInfoCollector);
    ChildExecutor testExecutor = new ChildExecutor(dynamicTestExecutor);
    context.getRunner().runParameterizedFeature(context, testExecutor);
    errorInfoCollector.assertEmpty();
    if (testExecutor.getExecutionCount() < 1) {
     throw new SpockExecutionException("Data provider has no data");
    }
    return context;
  }

  @Override
  public Type getType() {
    return Type.CONTAINER;
  }

  class ChildExecutor implements DynamicTestExecutor {

    private AtomicInteger executionCounter = new AtomicInteger();

    private final DynamicTestExecutor delegate;

    ChildExecutor(DynamicTestExecutor delegate) {this.delegate = delegate;}

    @Override
    public void execute(TestDescriptor testDescriptor) {
      executionCounter.incrementAndGet();
      addChild(testDescriptor);
      delegate.execute(testDescriptor);
    }

    @Override
    public void awaitFinished() throws InterruptedException {
      delegate.awaitFinished();
    }

    int getExecutionCount() {
      return executionCounter.get();
    }
  }
}
