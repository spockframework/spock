package org.spockframework.runtime;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.launcher.TestPlan;
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

  /**
   * Prevent this node from being pruned. The default logic would prune it, as it
   * <ul>
   *   <li>is no test,</li>
   *   <li>has no test descendents and</li>
   *   <li>may not register new tests during execution</li>
   * </ul>
   *
   * Without this empty override, the node is thrown away and the error is not reported.
   */
  @Override
  public void prune() {}

  /**
   *  Prevent this node from being removed. As it does not have any children, it would get removed when trying to select
   *  specific tests methods. For example, Gradle will report that no test were found, and not report the actual error.
   */
  @Override
  public void removeFromHierarchy() {}

  /**
   * Maven Surefire removes nodes if {@link TestPlan#containsTests()} is <tt>false</tt>.
   * In order to avoid this, we return <tt>true</tt> here, because it makes {@link #containsTests(TestDescriptor)}
   * also return <tt>true</tt>, which in turn feeds into Maven's test plan.
   * This seems to work and not to bother Gradle either.
   *
   * @return always <tt>true</tt> for error spec nodes
   */
  @Override
  public boolean mayRegisterTests() {
    return true;
  }

  /**
   * Always throws the error specified in {@link #ErrorSpecNode(UniqueId, RunnerConfiguration, SpecInfo, Throwable)},
   * escalating it up the call stack.
   */
  @Override
  public SpockExecutionContext prepare(SpockExecutionContext context) throws Exception {
    return ExceptionUtil.sneakyThrow(error);
  }
}
