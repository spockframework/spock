package org.spockframework.runtime;

import org.spockframework.runtime.model.*;
import org.spockframework.runtime.model.parallel.ResourceAccessMode;
import org.spockframework.util.ExceptionUtil;
import spock.config.RunnerConfiguration;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.engine.*;
import org.junit.platform.engine.support.descriptor.*;
import org.junit.platform.engine.support.hierarchical.*;
import org.opentest4j.TestAbortedException;

public abstract class SpockNode<T extends SpecElementInfo<?,?>>
  extends AbstractTestDescriptor implements Node<SpockExecutionContext> {

  private final RunnerConfiguration configuration;
  private final T nodeInfo;

  protected SpockNode(UniqueId uniqueId, String displayName, TestSource source,
                      RunnerConfiguration configuration, T nodeInfo) {
    super(uniqueId, displayName, source);
    this.configuration = configuration;
    this.nodeInfo = nodeInfo;
  }

  public T getNodeInfo() {
    return nodeInfo;
  }

  public RunnerConfiguration getConfiguration() {
    return configuration;
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

  @Override
  public ExecutionMode getExecutionMode() {
    Optional<ExecutionMode> executionMode = getExplicitExecutionMode();
    if (executionMode.isPresent()) {
      return executionMode.get();
    }
    Optional<TestDescriptor> parent = getParent();
    while (parent.isPresent() && parent.get() instanceof SpockNode) {
      SpockNode<?> spockParent = (SpockNode<?>) parent.get();
      executionMode = spockParent.getExplicitExecutionMode();
      if (executionMode.isPresent()) {
        return executionMode.get();
      }
      executionMode = spockParent.getDefaultChildExecutionMode();
      if (executionMode.isPresent()) {
        return executionMode.get();
      }
      parent = spockParent.getParent();
    }
    return toExecutionMode(configuration.parallel.defaultExecutionMode);
  }

  protected Optional<ExecutionMode> getDefaultChildExecutionMode() {
    return Optional.empty();
  }

  protected Optional<ExecutionMode> getExplicitExecutionMode() {
    return nodeInfo.getExecutionMode().map(SpockNode::toExecutionMode);
  }

  @Override
  public Set<ExclusiveResource> getExclusiveResources() {
    return toExclusiveResources(nodeInfo.getExclusiveResources());
  }

  protected static MethodSource featureToMethodSource(FeatureInfo info) {
    return MethodSource.from(info.getSpec().getBottomSpec().getReflection().getName(),
      info.getName(),
      ClassUtils.nullSafeToString(info.getFeatureMethod().getReflection().getParameterTypes()) // TODO replace internal API
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
