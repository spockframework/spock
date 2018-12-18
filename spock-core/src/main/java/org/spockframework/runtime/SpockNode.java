package org.spockframework.runtime;

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
}
