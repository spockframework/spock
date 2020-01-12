package org.spockframework.runtime;

import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.ExceptionUtil;

import org.junit.platform.engine.UniqueId;

public class ErrorSpecNode extends SpecNode {
  private final Throwable error;

  protected ErrorSpecNode(UniqueId uniqueId, SpecInfo specInfo, Throwable error) {
    super(uniqueId, specInfo);
    this.error = error;
  }

  @Override
  public SpockExecutionContext execute(SpockExecutionContext context, DynamicTestExecutor dynamicTestExecutor) throws Exception {
    ExceptionUtil.sneakyThrow(error);
    return null; //
  }
}
