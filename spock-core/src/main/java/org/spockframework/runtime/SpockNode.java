package org.spockframework.runtime;

import org.spockframework.runtime.model.*;
import org.spockframework.runtime.model.parallel.ResourceAccessMode;
import org.spockframework.util.ExceptionUtil;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.engine.*;
import org.junit.platform.engine.support.descriptor.*;
import org.junit.platform.engine.support.hierarchical.*;
import org.opentest4j.TestAbortedException;

public abstract class SpockNode<T extends SpecElementInfo<?,?>>
  extends AbstractTestDescriptor implements Node<SpockExecutionContext> {

  private final T nodeInfo;

  protected SpockNode(UniqueId uniqueId, String displayName, TestSource source, T nodeInfo) {
    super(uniqueId, displayName, source);
    this.nodeInfo = nodeInfo;
  }

  public T getNodeInfo() {
    return nodeInfo;
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

  protected Optional<ExecutionMode> getExplicitExecutionMode() {
    return nodeInfo.getExecutionMode().map(SpockNode::toExecutionMode);
  }

  @Override
  public Set<ExclusiveResource> getExclusiveResources() {
    return toExclusiveResources(nodeInfo.getExclusiveResources());
  }

  protected static MethodSource featureToMethodSource(FeatureInfo info) {
    return MethodSource.from(info.getSpec().getReflection().getName(),
      info.getName(),
      ClassUtils.nullSafeToString(info.getFeatureMethod().getReflection().getParameterTypes()) // TODO replace interal API
    );
  }

  protected static ExecutionMode toExecutionMode(org.spockframework.runtime.model.parallel.ExecutionMode mode) {
    switch (mode) {
      case CONCURRENT:
        return ExecutionMode.CONCURRENT;
      case SAME_THREAD:
        return ExecutionMode.SAME_THREAD;

      default:
      throw new SpockExecutionException("Unknown ExecutionMode: " + mode);
    }
  }

  protected static ExclusiveResource.LockMode toLockMode(ResourceAccessMode mode) {
    switch (mode) {
      case READ_WRITE:
        return ExclusiveResource.LockMode.READ_WRITE;
      case READ:
        return ExclusiveResource.LockMode.READ;

      default:
      throw new SpockExecutionException("Unknown ResourceAccessMode: " + mode);
    }
  }

  protected static Set<ExclusiveResource> toExclusiveResources(
    Collection<org.spockframework.runtime.model.parallel.ExclusiveResource> exclusiveResources) {

    return exclusiveResources.stream()
      .map(er -> new ExclusiveResource(er.getKey(), toLockMode(er.getMode())))
      .collect(Collectors.toSet());
  }
}
