package org.spockframework.runtime;

import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.ExceptionUtil;
import spock.config.RunnerConfiguration;

import org.junit.platform.engine.UniqueId;

public class ErrorSpecNode extends SpecNode {
  private final Throwable error;

  protected ErrorSpecNode(UniqueId uniqueId, RunnerConfiguration configuration, SpecInfo specInfo, Throwable error) {
    super(uniqueId, configuration, specInfo);
    this.error = error;
  }

  @Override
  public void prune() {
    // prevent pruning of this node
    // default logic would prune it as it
    // - is no test,
    // - has no test descendents and
    // - may not register new tests during execution
    // without this empty override, the node is thrown away and the error is not reported
  }

  @Override
  public SpockExecutionContext execute(SpockExecutionContext context, DynamicTestExecutor dynamicTestExecutor) throws Exception {
    return ExceptionUtil.sneakyThrow(error);
  }
}
