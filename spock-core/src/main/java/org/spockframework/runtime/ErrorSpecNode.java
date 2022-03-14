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
  public void removeFromHierarchy() {
    // prevent removal of this node
    // As the ErrorSpecNode does not have children it would get removed when trying to select specific tests methods.
    // For example, gradle will report that no test were found, and not report the actual error.
  }

  @Override
  public boolean mayRegisterTests() {
    // Maven Surefire removes nodes if org.junit.platform.launcher.TestPlan.containsTests is false.
    // The easiest way to avoid this, is to return true here. This seems to work and not to bother Gradle either.
    return true;
  }

  @Override
  public SpockExecutionContext prepare(SpockExecutionContext context) throws Exception {
    return ExceptionUtil.sneakyThrow(error);
  }
}
