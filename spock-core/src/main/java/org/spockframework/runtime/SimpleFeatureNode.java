package org.spockframework.runtime;

import org.spockframework.runtime.model.FeatureInfo;

import org.junit.platform.engine.*;
import org.junit.platform.engine.support.descriptor.MethodSource;

/**
 * A non-parametric feature (test) that only has a single "iteration".
 * Contrary to parametric tests - where the iterations are the children - this is the actual test. The execution
 * is only delegated, but does not cause any extra test events for the single iteration.
 */
public class SimpleFeatureNode extends FeatureNode {
  private final IterationNode delegate;

  public SimpleFeatureNode(UniqueId uniqueId, FeatureInfo featureInfo, IterationNode delegate) {
    super(uniqueId, featureInfo.getName(),  MethodSource.from(featureInfo.getFeatureMethod().getReflection()), featureInfo);
    this.delegate = delegate;
  }

  @Override
  public Type getType() {
    return delegate.getType();
  }


  @Override
  public SpockExecutionContext prepare(SpockExecutionContext context) throws Exception {
    // return context.withCurrentFeature(featureInfo); //.withParentId(getUniqueId())
    return delegate.prepare(context.withCurrentFeature(featureInfo));
  }

  @Override
  public SpockExecutionContext before(SpockExecutionContext context) throws Exception {
    context.getRunner().runSetup(context);
    return context;
  }

  @Override
  public SpockExecutionContext execute(SpockExecutionContext context, DynamicTestExecutor dynamicTestExecutor) throws Exception {
    // context.getRunner().runFeatureMethod(context);
    delegate.execute(context, dynamicTestExecutor);
    return context;
  }

  @Override
  public void after(SpockExecutionContext context) throws Exception {
    delegate.after(context);
  }

  @Override
  public void around(SpockExecutionContext context, Invocation<SpockExecutionContext> invocation) {
    delegate.around(context, invocation);
  }

  @Override
  public SkipResult shouldBeSkipped(SpockExecutionContext context) throws Exception {
    return delegate.shouldBeSkipped(context);
  }

}
