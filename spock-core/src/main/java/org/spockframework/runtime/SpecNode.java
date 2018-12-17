package org.spockframework.runtime;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.runner.notification.RunNotifier;
import org.spockframework.runtime.model.ErrorInfo;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.IterationInfo;
import org.spockframework.runtime.model.SpecInfo;

public class SpecNode extends SpockNode {
  private final SpecInfo specInfo;

  protected SpecNode(UniqueId uniqueId, SpecInfo specInfo) {
    super(uniqueId, specInfo.getName(), ClassSource.from(specInfo.getReflection()));
    this.specInfo = specInfo;

  }

  @Override
  public Type getType() {
    return Type.CONTAINER;
  }

  @Override
  public SpockExecutionContext prepare(SpockExecutionContext context) throws Exception {
    PlatformParameterizedSpecRunner specRunner = context.getRunContext().createSpecRunner(specInfo, new NoopRunSupervisor());
    context = context.withRunner(specRunner);
    return specRunner.runSharedSpec(context);
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

  private static class NoopRunSupervisor extends RunNotifier implements IRunSupervisor {
    @Override
    public void beforeSpec(SpecInfo spec) {

    }

    @Override
    public void beforeFeature(FeatureInfo feature) {

    }

    @Override
    public void beforeIteration(IterationInfo iteration) {

    }

    @Override
    public void afterIteration(IterationInfo iteration) {

    }

    @Override
    public void afterFeature(FeatureInfo feature) {

    }

    @Override
    public void afterSpec(SpecInfo spec) {

    }

    @Override
    public int error(ErrorInfo error) {
      return 0;
    }

    @Override
    public void specSkipped(SpecInfo spec) {

    }

    @Override
    public void featureSkipped(FeatureInfo feature) {

    }
  }
}
