package org.spockframework.runtime;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

public class SpockEngineDescriptor extends EngineDescriptor implements Node<SpockExecutionContext> {
  private final RunContext runContext;

  public SpockEngineDescriptor(UniqueId uniqueId, RunContext runContext) {
    super(uniqueId, "Spock");
    this.runContext = runContext;
  }

  @Override
  public SpockExecutionContext prepare(SpockExecutionContext context) throws Exception {
    return context.withRunContext(runContext);
  }

  @Override
  public void after(SpockExecutionContext context) throws Exception {
    runContext.stop();
  }

  RunContext getRunContext() {
    return runContext;
  }
}
