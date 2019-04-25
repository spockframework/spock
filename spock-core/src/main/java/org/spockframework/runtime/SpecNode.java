package org.spockframework.runtime;

import org.spockframework.runtime.model.SpecInfo;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

public class SpecNode extends SpockNode {

  private final SpecInfo specInfo;

  protected SpecNode(UniqueId uniqueId, SpecInfo specInfo) {
    super(uniqueId, specInfo.getName(), ClassSource.from(specInfo.getReflection()));
    this.specInfo = specInfo;
  }

  public SpecInfo getSpecInfo() {
    return specInfo;
  }

  @Override
  public Type getType() {
    return Type.CONTAINER;
  }

  @Override
  public SpockExecutionContext prepare(SpockExecutionContext context) throws Exception {
    PlatformParameterizedSpecRunner specRunner = context.getRunContext().createSpecRunner(specInfo);
    context = context.withRunner(specRunner).withSpec(specInfo);
    return specRunner.runSharedSpec(context);
  }

  @Override
  public SkipResult shouldBeSkipped(SpockExecutionContext context) throws Exception {
    return shouldBeSkipped(specInfo);
  }

  @Override
  public SpockExecutionContext before(SpockExecutionContext context) throws Exception {
    context.getRunner().runSetupSpec(context);
    return context;
  }

  @Override
  public void after(SpockExecutionContext context) throws Exception {
    context.getRunner().runCleanupSpec(context);
  }

  @Override
  public void around(SpockExecutionContext context, Invocation<SpockExecutionContext> invocation) throws Exception {
    context.getRunner().runSpec(context, () -> sneakyInvoke(invocation, context));
  }

}
