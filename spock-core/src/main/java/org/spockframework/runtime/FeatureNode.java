package org.spockframework.runtime;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.spockframework.runtime.model.FeatureInfo;

public class FeatureNode extends SpockNode {
  private final FeatureInfo featureInfo;

  protected FeatureNode(UniqueId uniqueId, FeatureInfo featureInfo) {
    super(uniqueId, featureInfo.getName(), MethodSource.from(featureInfo.getFeatureMethod().getReflection()));
    this.featureInfo = featureInfo;
  }

  @Override
  public Type getType() {
    return Type.TEST;
  }

  @Override
  public SpockExecutionContext prepare(SpockExecutionContext context) throws Exception {
    return context.withCurrentInstance(createSpecInstance(featureInfo.getSpec()));
  }

  @Override
  public SpockExecutionContext before(SpockExecutionContext context) throws Exception {
//    context.getRunner().d;
    return context;
  }

  @Override
  public SpockExecutionContext execute(SpockExecutionContext context, DynamicTestExecutor dynamicTestExecutor) throws Exception {
    context.getRunner().runFeature(context.withCurrentFeature(featureInfo));
    return context;
  }

  @Override
  public void after(SpockExecutionContext context) throws Exception {

  }

  @Override
  public SkipResult shouldBeSkipped(SpockExecutionContext context) throws Exception {

    return featureInfo.isSkipped() ? SkipResult.skip("because I said so") : SkipResult.doNotSkip();
  }


}
