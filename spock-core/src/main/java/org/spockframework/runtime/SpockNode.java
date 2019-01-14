package org.spockframework.runtime;

import org.spockframework.util.ExceptionUtil;

import org.junit.platform.engine.*;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.Node;

public abstract class SpockNode extends AbstractTestDescriptor implements Node<SpockExecutionContext> {

  protected SpockNode(UniqueId uniqueId, String displayName) {
    super(uniqueId, displayName);
  }

  protected SpockNode(UniqueId uniqueId, String displayName, TestSource source) {
    super(uniqueId, displayName, source);
  }

  protected void sneakyInvoke(Invocation<SpockExecutionContext> invocation, SpockExecutionContext context) {
    try {
      invocation.invoke(context);
    } catch (Exception e) {
      ExceptionUtil.sneakyThrow(e);
    }
  }
}
