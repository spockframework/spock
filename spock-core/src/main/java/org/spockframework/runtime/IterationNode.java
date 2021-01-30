package org.spockframework.runtime;

import org.spockframework.runtime.model.*;
import spock.config.RunnerConfiguration;

import java.util.*;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource;

public class IterationNode extends SpockNode<FeatureInfo> {
  private final IterationInfo iterationInfo;

  protected IterationNode(UniqueId uniqueId, RunnerConfiguration configuration, IterationInfo iterationInfo) {
    super(uniqueId, iterationInfo.getDisplayName(), featureToMethodSource(iterationInfo.getFeature()), configuration,
      iterationInfo.getFeature());
    this.iterationInfo = iterationInfo;
  }

  @Override
  public SpockExecutionContext prepare(SpockExecutionContext context) throws Exception {
    if (iterationInfo.getFeature().isSkipped()) {
      // Node.prepare is called before Node.shouldBeSkipped so we just skip the prepare
      return context;
    }
    context.getErrorInfoCollector().assertEmpty();
    context = context.withCurrentIteration(iterationInfo);
    context = context.getRunner().createSpecInstance(context, false);
    context.getRunner().runInitializer(context);
    context.getErrorInfoCollector().assertEmpty();
    return context;
  }

  @Override
  public SpockExecutionContext before(SpockExecutionContext context) throws Exception {
    ErrorInfoCollector errorInfoCollector = new ErrorInfoCollector();
    context = context.withErrorInfoCollector(errorInfoCollector);
    context.getRunner().runSetup(context);
    errorInfoCollector.assertEmpty();
    return context;
  }

  @Override
  public SpockExecutionContext execute(SpockExecutionContext context, DynamicTestExecutor dynamicTestExecutor) throws Exception {
    verifyNotSkipped(iterationInfo.getFeature());
    ErrorInfoCollector errorInfoCollector = new ErrorInfoCollector();
    context = context.withErrorInfoCollector(errorInfoCollector);
    context.getRunner().runFeatureMethod(context);
    errorInfoCollector.assertEmpty();
    return context;
  }

  @Override
  public void after(SpockExecutionContext context) throws Exception {
    ErrorInfoCollector errorInfoCollector = new ErrorInfoCollector();
    context = context.withErrorInfoCollector(errorInfoCollector);
    context.getRunner().runCleanup(context);
    errorInfoCollector.assertEmpty();
  }

  @Override
  public void around(SpockExecutionContext context, Invocation<SpockExecutionContext> invocation) {
    context.getRunner().runIteration(context, iterationInfo, () -> sneakyInvoke(invocation, context));
  }

  @Override
  public Type getType() {
    return Type.TEST;
  }

  @Override
  public SkipResult shouldBeSkipped(SpockExecutionContext context) throws Exception {
    return shouldBeSkipped(iterationInfo.getFeature());
  }

  @Override
  public Set<ExclusiveResource> getExclusiveResources() {
    return Collections.emptySet();
  }
}
