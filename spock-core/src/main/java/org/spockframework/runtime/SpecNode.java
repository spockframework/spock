package org.spockframework.runtime;

import org.junit.platform.engine.TestDescriptor;
import org.spockframework.runtime.model.SpecInfo;
import spock.config.RunnerConfiguration;

import java.util.Optional;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;

public class SpecNode extends SpockNode<SpecInfo> {

  protected SpecNode(UniqueId uniqueId, RunnerConfiguration configuration, SpecInfo specInfo) {
    super(uniqueId, specInfo.getDisplayName(), ClassSource.from(specInfo.getReflection()), configuration, specInfo);
  }

  @Override
  public Type getType() {
    return Type.CONTAINER;
  }

  @Override
  public SpockExecutionContext prepare(SpockExecutionContext context) throws Exception {
    if (getNodeInfo().isSkipped()) {
      // Node.prepare is called before Node.shouldBeSkipped so we just skip the shared spec initialization
      return context;
    }
    PlatformParameterizedSpecRunner specRunner = context.getRunContext().createSpecRunner(getNodeInfo());
    ErrorInfoCollector errorInfoCollector = new ErrorInfoCollector();
    context = context.withErrorInfoCollector(errorInfoCollector);
    context = context.withRunner(specRunner).withSpec(getNodeInfo());
    context = specRunner.runSharedSpec(context);
    errorInfoCollector.assertEmpty();
    return context;
  }

  @Override
  public SkipResult shouldBeSkipped(SpockExecutionContext context) throws Exception {
    return shouldBeSkipped(getNodeInfo());
  }

  @Override
  public SpockExecutionContext before(SpockExecutionContext context) throws Exception {
    ErrorInfoCollector errorInfoCollector = new ErrorInfoCollector();
    context = context.withErrorInfoCollector(errorInfoCollector);
    context.getRunner().runSetupSpec(context);
    errorInfoCollector.assertEmpty();
    return context;
  }

  @Override
  public void after(SpockExecutionContext context) throws Exception {
    ErrorInfoCollector errorInfoCollector = new ErrorInfoCollector();
    context = context.withErrorInfoCollector(errorInfoCollector);
    context.getRunner().runCleanupSpec(context);
    errorInfoCollector.assertEmpty();
  }

  @Override
  public void around(SpockExecutionContext context, Invocation<SpockExecutionContext> invocation) throws Exception {
    ErrorInfoCollector errorInfoCollector = new ErrorInfoCollector();
    SpockExecutionContext ctx = context.withErrorInfoCollector(errorInfoCollector);
    ctx.getRunner().runSpec(ctx, () -> sneakyInvoke(invocation, ctx));
    errorInfoCollector.assertEmpty();
  }

  @Override
  public void nodeSkipped(SpockExecutionContext context, TestDescriptor testDescriptor, SkipResult result) {
    SpecInfo specInfo = getNodeInfo();
    specInfo.getListeners().forEach(iRunListener -> iRunListener.specSkipped(specInfo));
  }

  @Override
  public ExecutionMode getExecutionMode() {
    return getExplicitExecutionMode()
      .orElseGet( ()-> toExecutionMode(getConfiguration().parallel.defaultSpecificationExecutionMode));
  }

  @Override
  protected Optional<ExecutionMode> getDefaultChildExecutionMode() {
    return getNodeInfo().getChildExecutionMode().map(SpockNode::toExecutionMode);
  }
}
