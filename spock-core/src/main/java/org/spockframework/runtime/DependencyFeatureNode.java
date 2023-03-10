package org.spockframework.runtime;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.spockframework.runtime.model.FeatureInfo;

import java.util.Optional;

/**
 * A feature that is a dependency of one or multiple other features, depends on one or multiple other features, or both.
 * The actual test work is delegated to another feature node.
 * This node makes sure the node is not removed from hierarchy e.g. due to filtering as long as one dependee is still present.
 */
public class DependencyFeatureNode extends FeatureNode {

  private final FeatureNode delegate;

  private boolean removedFromHierarchy;

  public DependencyFeatureNode(FeatureNode delegate) {
    super(delegate);
    this.delegate = delegate;
  }

  @Override
  public Type getType() {
    return delegate.getType();
  }

  @Override
  public SpockExecutionContext prepare(SpockExecutionContext context) throws Exception {
    return delegate.prepare(context);
  }

  @Override
  public SpockExecutionContext before(SpockExecutionContext context) throws Exception {
    return delegate.before(context);
  }

  @Override
  public void around(SpockExecutionContext context, Invocation<SpockExecutionContext> invocation) {
    delegate.around(context, invocation);
  }

  @Override
  public SpockExecutionContext execute(SpockExecutionContext context, DynamicTestExecutor dynamicTestExecutor) throws Exception {
    return delegate.execute(context, dynamicTestExecutor);
  }

  @Override
  public void after(SpockExecutionContext context) throws Exception {
    delegate.after(context);
  }

  @Override
  public void nodeFinished(SpockExecutionContext context, TestDescriptor testDescriptor, TestExecutionResult result) {
    delegate.nodeFinished(context, testDescriptor, result);
  }

  @Override
  public void nodeSkipped(SpockExecutionContext context, TestDescriptor testDescriptor, SkipResult result) {
    delegate.nodeSkipped(context, testDescriptor, result);
  }

  @Override
  public SkipResult shouldBeSkipped(SpockExecutionContext context) throws Exception {
    return delegate.shouldBeSkipped(context);
  }

  private boolean allDependeesAreRemoved() {
    return getNodeInfo()
      .getDependees()
      .stream()
      .map(FeatureInfo::getNode)
      .map(DependencyFeatureNode.class::cast)
      .map(TestDescriptor::getParent)
      .noneMatch(Optional::isPresent);
  }

  @Override
  public void removeFromHierarchy() {
    if (getNodeInfo().getDependees().isEmpty() || allDependeesAreRemoved()) {
      super.removeFromHierarchy();
      // retry to remove dependencies that were just preserved for this dependee
      getNodeInfo()
        .getDependencies()
        .stream()
        .map(FeatureInfo::getNode)
        .map(DependencyFeatureNode.class::cast)
        .filter(dependency -> dependency.removedFromHierarchy)
        .forEach(TestDescriptor::removeFromHierarchy);
    } else {
      // do not remove from hierarchy but remember it was removed
      removedFromHierarchy = true;
    }
  }
}
