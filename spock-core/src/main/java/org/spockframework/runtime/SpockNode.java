package org.spockframework.runtime;

import org.spockframework.runtime.model.*;
import org.spockframework.util.ExceptionUtil;

import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.engine.*;
import org.junit.platform.engine.support.descriptor.*;
import org.junit.platform.engine.support.hierarchical.Node;
import org.opentest4j.TestAbortedException;

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

  protected SkipResult shouldBeSkipped(ISkippable skippable) {
    return skippable.isSkipped() ? SkipResult.skip(skippable.getSkipReason()) : SkipResult.doNotSkip();
  }

  protected void verifyNotSkipped(ISkippable skippable) {
    if (skippable.isSkipped()) {
      throw new TestAbortedException(skippable.getSkipReason());
    }
  }

  protected static MethodSource featureToMethodSource(FeatureInfo info) {
    return MethodSource.from(info.getSpec().getReflection().getName(),
      info.getName(),
      ClassUtils.nullSafeToString(info.getFeatureMethod().getReflection().getParameterTypes()) // TODO replace interal API
    );
  }
}
