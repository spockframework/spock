package org.spockframework.runtime;

import org.spockframework.runtime.model.FeatureInfo;
import spock.config.RunnerConfiguration;

import org.junit.platform.engine.*;

public abstract class FeatureNode extends SpockNode<FeatureInfo> {

  public FeatureNode(UniqueId uniqueId, String displayName, TestSource source, RunnerConfiguration configuration,
                     FeatureInfo featureInfo) {
    super(uniqueId, displayName, source, configuration, featureInfo);
    tags = getTags(featureInfo.getFeatureMethod().getReflection());
  }

  @Override
  public Type getType() {
    return Type.CONTAINER;
  }

  @Override
  public SkipResult shouldBeSkipped(SpockExecutionContext context) throws Exception {
    return shouldBeSkipped(getNodeInfo());
  }

  @Override
  public void around(SpockExecutionContext context, Invocation<SpockExecutionContext> invocation) {
    ErrorInfoCollector errorInfoCollector = new ErrorInfoCollector();
    final SpockExecutionContext innerContext = context.withErrorInfoCollector(errorInfoCollector);
    context.getRunner().runFeature(innerContext, () -> sneakyInvoke(invocation, innerContext));
    errorInfoCollector.assertEmpty();
  }

  @Override
  public boolean mayRegisterTests() {
    return getNodeInfo().isParameterized();
  }
}
