package org.spockframework.runtime;

import org.spockframework.runtime.model.FeatureInfo;

import org.junit.platform.engine.*;
import org.junit.platform.engine.support.descriptor.MethodSource;

public class ParameterizedFeatureNode extends FeatureNode {

  protected ParameterizedFeatureNode(UniqueId uniqueId, FeatureInfo featureInfo) {
    super(uniqueId, featureInfo.getName(), MethodSource.from(featureInfo.getFeatureMethod().getReflection()), featureInfo);
  }


  @Override
  public SpockExecutionContext prepare(SpockExecutionContext context) throws Exception {
    featureInfo.setIterationNameProvider(new SafeIterationNameProvider(featureInfo.getIterationNameProvider()));
    return context.withCurrentFeature(featureInfo).withParentId(getUniqueId());
  }

  @Override
  public SpockExecutionContext execute(SpockExecutionContext context, DynamicTestExecutor dynamicTestExecutor) throws Exception {
    verifyNotSkipped(featureInfo);
    context.getRunner().runParameterizedFeature(context, new ChildExecutor(dynamicTestExecutor));
    // todo assert at least one test execution
    return context;
  }

  @Override
  public Type getType() {
    return Type.CONTAINER;
  }

  class ChildExecutor implements DynamicTestExecutor {

    private final DynamicTestExecutor delegate;

    ChildExecutor(DynamicTestExecutor delegate) {this.delegate = delegate;}

    @Override
    public void execute(TestDescriptor testDescriptor) {
      // TODO count test
      addChild(testDescriptor);
      delegate.execute(testDescriptor);

    }

    @Override
    public void awaitFinished() throws InterruptedException {
      delegate.awaitFinished();
    }
  }
}
