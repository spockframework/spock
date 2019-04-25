package org.spockframework.runtime;

import org.spockframework.runtime.model.FeatureInfo;

import org.junit.platform.engine.*;

public abstract class FeatureNode extends SpockNode {
  protected final FeatureInfo featureInfo;

  public FeatureNode(UniqueId uniqueId, String displayName, TestSource source, FeatureInfo featureInfo) {
    super(uniqueId, displayName, source);
    this.featureInfo = featureInfo;
  }

  @Override
  public Type getType() {
    return Type.CONTAINER;
  }

  @Override
  public SkipResult shouldBeSkipped(SpockExecutionContext context) throws Exception {
    return shouldBeSkipped(featureInfo);
  }

  @Override
  public void around(SpockExecutionContext context, Invocation<SpockExecutionContext> invocation) {
    context.getRunner().runFeature(context, () -> sneakyInvoke(invocation, context));
  }

  @Override
  public boolean mayRegisterTests() {
    return featureInfo.isParameterized();
  }
}
