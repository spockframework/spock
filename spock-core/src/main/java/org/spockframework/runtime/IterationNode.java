package org.spockframework.runtime;

import org.spockframework.runtime.model.IterationInfo;

import java.util.function.Consumer;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;

public class IterationNode extends SpockNode {
  private final IterationInfo iterationInfo;

  protected IterationNode(UniqueId uniqueId, IterationInfo iterationInfo) {
    super(uniqueId, iterationInfo.getName(),
      MethodSource.from(iterationInfo.getFeature().getFeatureMethod().getReflection()));
    this.iterationInfo = iterationInfo;
  }

  @Override
  public SpockExecutionContext prepare(SpockExecutionContext context) throws Exception {
    context = context.withCurrentIteration(iterationInfo);
    context = context.getRunner().createSpecInstance(context, false);
    context.getRunner().runInitializer(context);
    return context;
  }

  @Override
  public SpockExecutionContext before(SpockExecutionContext context) throws Exception {
    context.getRunner().runSetup(context);
    return context;
  }

  @Override
  public SpockExecutionContext execute(SpockExecutionContext context, DynamicTestExecutor dynamicTestExecutor) throws Exception {
    context.getRunner().runFeatureMethod(context);
    return context;
  }

  @Override
  public void after(SpockExecutionContext context) throws Exception {
    context.getRunner().runCleanup(context);
  }

  @Override
  public void around(SpockExecutionContext context, Consumer<SpockExecutionContext> runnable) {
    context.getRunner().runIteration(context, iterationInfo, () -> runnable.accept(context));
  }

  @Override
  public Type getType() {
    return Type.TEST;
  }

  @Override
  public SkipResult shouldBeSkipped(SpockExecutionContext context) throws Exception {
    return iterationInfo.getFeature().isSkipped() ? SkipResult.skip("because I said so") : SkipResult.doNotSkip();
  }
}
