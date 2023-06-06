package org.spockframework.runtime;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

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
    ThrowableCollector collector = new ThrowableCollector(__ -> false);
    collector.execute(runContext::stop);
    collector.execute(context.getStoreProvider()::close);
    collector.assertEmpty();
  }

  RunContext getRunContext() {
    return runContext;
  }
}
