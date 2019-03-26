package org.spockframework.runtime;

import org.spockframework.runtime.model.FeatureInfo;

import org.junit.platform.engine.*;
import org.junit.platform.engine.support.descriptor.MethodSource;

/**
 * A non-parametric feature (test) that only has a single "iteration".
 * Contrary to parametric tests - where the iterations are the children - this is the actual test. The execution
 * is only delegated, but does not cause any extra test events for the single iteration.
 * <p>
 * All node events are also delegated to the {@link IterationNode} in the correct order.
 */
public class SimpleFeatureNode extends FeatureNode {

  private final IterationNode delegate;

  public SimpleFeatureNode(UniqueId uniqueId, FeatureInfo featureInfo, IterationNode delegate) {
    super(uniqueId, featureInfo.getName(), MethodSource.from(featureInfo.getFeatureMethod().getReflection()), featureInfo);
    this.delegate = delegate;
  }

  @Override
  public Type getType() {
    return delegate.getType();
  }

  @Override
  public SpockExecutionContext prepare(SpockExecutionContext context) throws Exception {
    return delegate.prepare(
      context.withCurrentFeature(featureInfo)
      //.withParentId(getUniqueId())
    );
  }

  @Override
  public SpockExecutionContext before(SpockExecutionContext context) throws Exception {
    context = super.before(context);
    context.getRunner().runSetup(context);
    return context;
  }

  @Override
  public void around(SpockExecutionContext context, Invocation<SpockExecutionContext> invocation) {
    // Wrap the Feature invocation around the invocation of the Iteration delegate
    super.around(context, ctx -> {
      delegate.around(ctx, invocation);
    });
  }

  @Override
  public SpockExecutionContext execute(SpockExecutionContext context, DynamicTestExecutor dynamicTestExecutor) throws Exception {
    delegate.execute(context, dynamicTestExecutor);
    return context;
  }

  @Override
  public void after(SpockExecutionContext context) throws Exception {
    delegate.after(context);
    // First the iteration node, then the Feature node
    super.after(context);
  }

  @Override
  public void nodeFinished(SpockExecutionContext context, TestDescriptor testDescriptor, TestExecutionResult result) {
    delegate.nodeFinished(context, testDescriptor, result);
    super.nodeFinished(context, testDescriptor, result);
  }

  @Override
  public void nodeSkipped(SpockExecutionContext context, TestDescriptor testDescriptor, SkipResult result) {
    // Skipping this Feature implies that the Iteration is also skipped
    delegate.nodeSkipped(context, testDescriptor, result);
    super.nodeSkipped(context, testDescriptor, result);
  }

  @Override
  public SkipResult shouldBeSkipped(SpockExecutionContext context) throws Exception {
    return delegate.shouldBeSkipped(context);
  }

}
