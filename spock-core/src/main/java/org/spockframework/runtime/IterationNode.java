package org.spockframework.runtime;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.IterationInfo;
import spock.config.RunnerConfiguration;

import java.util.Set;

import static java.util.Collections.emptySet;

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
    context.runAndAssertWithNewErrorCollector(context.getRunner()::runInitializer);
    return context;
  }

  @Override
  public SpockExecutionContext before(SpockExecutionContext context) throws Exception {
    context.runAndAssertWithNewErrorCollector(context.getRunner()::runSetup);
    return context;
  }

  @Override
  public SpockExecutionContext execute(SpockExecutionContext context, DynamicTestExecutor dynamicTestExecutor) throws Exception {
    verifyNotSkipped(iterationInfo.getFeature());
    context.runAndAssertWithNewErrorCollector(context.getRunner()::runFeatureMethod);
    return context;
  }

  @Override
  public void after(SpockExecutionContext context) throws Exception {
    context.runAndAssertWithNewErrorCollector(context.getRunner()::runCleanup);
  }

  @Override
  public void around(SpockExecutionContext context, Invocation<SpockExecutionContext> invocation) {
    context.runAndAssertWithNewErrorCollector(ctx -> ctx.getRunner().runIteration(ctx, iterationInfo, () -> sneakyInvoke(invocation, ctx)));
  }

  @Override
  public Type getType() {
    return Type.TEST;
  }

  @Override
  public SkipResult shouldBeSkipped(SpockExecutionContext context) {
    return shouldBeSkipped(iterationInfo.getFeature());
  }

  @Override
  public Set<ExclusiveResource> getExclusiveResources() {
    return emptySet();
  }
}
