package org.spockframework.runtime;

import org.spockframework.runtime.model.FeatureInfo;

import org.junit.platform.engine.*;
import org.junit.platform.engine.support.descriptor.MethodSource;

public class FeatureNode extends SpockNode {
  private final FeatureInfo featureInfo;

  protected FeatureNode(UniqueId uniqueId, FeatureInfo featureInfo) {
    super(uniqueId, featureInfo.getName(), MethodSource.from(featureInfo.getFeatureMethod().getReflection()));
    this.featureInfo = featureInfo;
  }

  @Override
  public Type getType() {
    return Type.CONTAINER;
  }

  @Override
  public SpockExecutionContext prepare(SpockExecutionContext context) throws Exception {
    featureInfo.setIterationNameProvider(new SafeIterationNameProvider(featureInfo.getIterationNameProvider()));
    return context.withCurrentFeature(featureInfo).withParentId(getUniqueId());
  }

  @Override
  public SpockExecutionContext execute(SpockExecutionContext context, DynamicTestExecutor dynamicTestExecutor) throws Exception {
    context.getRunner().runParameterizedFeature(context, new ChildExecutor(dynamicTestExecutor));
    return context;
  }

  @Override
  public SkipResult shouldBeSkipped(SpockExecutionContext context) throws Exception {

    return featureInfo.isSkipped() ? SkipResult.skip("because I said so") : SkipResult.doNotSkip();
  }

  @Override
  public void around(SpockExecutionContext context, Invocation<SpockExecutionContext> invocation) {
    context.getRunner().runFeature(context, () -> sneakyInvoke(invocation, context));
  }

  @Override
  public boolean mayRegisterTests() {
    return true;
  }

  class ChildExecutor implements DynamicTestExecutor {

    private final DynamicTestExecutor delegate;

    ChildExecutor(DynamicTestExecutor delegate) {this.delegate = delegate;}

    @Override
    public void execute(TestDescriptor testDescriptor) {
      addChild(testDescriptor);
      delegate.execute(testDescriptor);

    }

    @Override
    public void awaitFinished() throws InterruptedException {
      delegate.awaitFinished();
    }
  }
}
