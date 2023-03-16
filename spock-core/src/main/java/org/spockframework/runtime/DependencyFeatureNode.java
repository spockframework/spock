package org.spockframework.runtime;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.spockframework.runtime.model.FeatureInfo;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

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
    return !findDependencyFeatureNodes(getRootDescriptor(this), getNodeInfo().getDependees())
      .findAny()
      .isPresent();
  }

  @Override
  public void removeFromHierarchy() {
    if (getNodeInfo().getDependees().isEmpty() || allDependeesAreRemoved()) {
      TestDescriptor rootDescriptor = getRootDescriptor(this);
      super.removeFromHierarchy();
      // retry to remove dependencies that were just preserved for this dependee
      findDependencyFeatureNodes(rootDescriptor, getNodeInfo().getDependencies())
        .filter(dependency -> dependency.removedFromHierarchy)
        .forEach(TestDescriptor::removeFromHierarchy);
    } else {
      // do not remove from hierarchy but remember it was removed
      removedFromHierarchy = true;
    }
  }

  private static TestDescriptor getRootDescriptor(TestDescriptor testDescriptor) {
    if (testDescriptor.isRoot()) {
      return testDescriptor;
    }
    return getRootDescriptor(testDescriptor.getParent().orElseThrow(AssertionError::new));
  }

  private static Stream<DependencyFeatureNode> findDependencyFeatureNodes(TestDescriptor rootDescriptor, List<FeatureInfo> featureInfos) {
    return featureInfos
      .stream()
      .map(featureInfo -> rootDescriptor
        .getDescendants()
        .stream()
        .map(SpockNode.class::cast)
        .filter(spockNode -> spockNode.getNodeInfo().equals(featureInfo))
        .map(DependencyFeatureNode.class::cast)
        .collect(collectingAndThen(toList(), spockNode -> {
          switch (spockNode.size()) {
            case 0:
              return null;

            case 1:
              return spockNode.get(0);

            default:
              throw new AssertionError("Expected to find 0 or 1 node, but found " + spockNode.size());
          }
        }))
      )
      .filter(Objects::nonNull);
  }
}
