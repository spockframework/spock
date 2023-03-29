package org.spockframework.runtime;

import org.spockframework.runtime.model.FeatureInfo;
import spock.config.RunnerConfiguration;

import org.junit.platform.engine.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public abstract class FeatureNode extends SpockNode<FeatureInfo> {

  protected boolean removedFromHierarchy = false;

  public FeatureNode(UniqueId uniqueId, String displayName, TestSource source, RunnerConfiguration configuration,
                     FeatureInfo featureInfo) {
    super(uniqueId, displayName, source, configuration, featureInfo);
  }

  @Override
  public Type getType() {
    return Type.CONTAINER;
  }

  @Override
  public Set<TestTag> getTags() {
    return getNodeInfo().getTestTags().stream()
      .map(org.spockframework.runtime.model.TestTag::getValue)
      .map(TestTag::create)
      .collect(toSet());
  }

  @Override
  public SkipResult shouldBeSkipped(SpockExecutionContext context) throws Exception {
    return shouldBeSkipped(getNodeInfo());
  }

  @Override
  public void around(SpockExecutionContext context, Invocation<SpockExecutionContext> invocation) {
    ErrorInfoCollector errorInfoCollector = new ErrorInfoCollector();
    final SpockExecutionContext innerContext = context.withErrorInfoCollector(errorInfoCollector);
    context.getRunner().runFeature(innerContext, () -> sneakyInvoke(invocation, innerContext));
    errorInfoCollector.assertEmpty();
  }

  @Override
  public boolean mayRegisterTests() {
    return getNodeInfo().isParameterized();
  }

  private boolean allImplyingFeaturesAreRemoved() {
    return !findFeatureNodes(getNodeInfo().getImplyingFeatures())
      .findAny()
      .isPresent();
  }

  @Override
  public void removeFromHierarchy() {
    if (getNodeInfo().getImplyingFeatures().isEmpty() || allImplyingFeaturesAreRemoved()) {
      Stream<? extends FeatureNode> impliedFeatureNodes = findFeatureNodes(getNodeInfo().getImpliedFeatures());
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

  private Stream<? extends  FeatureNode> findFeatureNodes(List<FeatureInfo> features) {
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
