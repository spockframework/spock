package org.spockframework.runtime;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.spockframework.runtime.model.FeatureInfo;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * A feature that is implied by one or multiple other features within the same specification,
 * implies one or multiple other features within the same specification,
 * or both.
 * The actual test work is delegated to another feature node.
 * This node makes sure the node is not removed from hierarchy e.g. due to filtering as long as one implying feature is still present.
 */
public class ImplicationFeatureNode extends FeatureNode {

  private final FeatureNode delegate;

  private boolean removedFromHierarchy;

  public ImplicationFeatureNode(FeatureNode delegate) {
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

  private boolean allImplyingFeaturesAreRemoved() {
    return !findImplicationFeatureNodes(getNodeInfo().getImplyingFeatures())
      .findAny()
      .isPresent();
  }

  @Override
  public void removeFromHierarchy() {
    if (getNodeInfo().getImplyingFeatures().isEmpty() || allImplyingFeaturesAreRemoved()) {
      Stream<ImplicationFeatureNode> impliedFeatureNodes = findImplicationFeatureNodes(getNodeInfo().getImpliedFeatures());
      super.removeFromHierarchy();
      // retry to remove implied features that were just preserved for this implying feature
      impliedFeatureNodes
        .filter(impliedFeature -> impliedFeature.removedFromHierarchy)
        .forEach(TestDescriptor::removeFromHierarchy);
    } else {
      // do not remove from hierarchy but remember it should have been removed
      removedFromHierarchy = true;
    }
  }

  private Stream<ImplicationFeatureNode> findImplicationFeatureNodes(List<FeatureInfo> features) {
    SpecNode specNode = getParent()
      .map(SpecNode.class::cast)
      .orElseThrow(AssertionError::new);

    return features
      .stream()
      .map(featureInfo -> specNode
        .getChildren()
        .stream()
        .map(FeatureNode.class::cast)
        .filter(featureNode -> featureNode.getNodeInfo().equals(featureInfo))
        .map(ImplicationFeatureNode.class::cast)
        .collect(collectingAndThen(toList(), featureNodes -> {
          switch (featureNodes.size()) {
            case 0:
              return null;

            case 1:
              return featureNodes.get(0);

            default:
              throw new AssertionError("Expected to find 0 or 1 node, but found multiple");
          }
        }))
      )
      .filter(Objects::nonNull);
  }
}
