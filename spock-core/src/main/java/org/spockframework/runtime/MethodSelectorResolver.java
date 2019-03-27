package org.spockframework.runtime;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.IterationInfo;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;

public class MethodSelectorResolver implements SelectorResolver {

  private static final Object[] EMPTY_ARGS = new Object[0];

  @Override
  public Set<Class<? extends DiscoverySelector>> getSupportedSelectorTypes() {
    return singleton(MethodSelector.class);
  }

  @Override
  public Optional<Result> resolveSelector(DiscoverySelector selector, Context context) {
    if (selector instanceof MethodSelector) {
      MethodSelector methodSelector = (MethodSelector) selector;
      return context
        .addToParent(() -> selectClass(methodSelector.getJavaClass()), parent -> {
          if (parent instanceof SpecNode) {
            String methodName = methodSelector.getMethodName();
            Optional<SpockNode> node = toNode((SpecNode) parent, feature -> feature.getFeatureMethod().getReflection().getName().equals(methodName));
            if (!node.isPresent()) {
              node = toNode((SpecNode) parent, feature -> methodName.equals(feature.getName()));
            }
            return node;
          }
          return Optional.empty();
        })
        .map(this::toResult);
    }
    return Optional.empty();
  }

  @Override
  public Optional<Result> resolveUniqueId(UniqueId uniqueId, Context context) {
    UniqueId.Segment lastSegment = uniqueId.getLastSegment();
    if ("feature".equals(lastSegment.getType())) {
      return context
        .addToParent(() -> selectUniqueId(uniqueId.removeLastSegment()), parent -> {
          if (parent instanceof SpecNode) {
            String methodName = lastSegment.getValue();
            return toNode((SpecNode) parent, feature -> methodName.equals(feature.getFeatureMethod().getReflection().getName()));
          }
          return Optional.empty();
        })
        .map(this::toResult);
    }
    if ("iteration".equals(lastSegment.getType())) {
      return context
        .addToParent(() -> selectUniqueId(uniqueId.removeLastSegment()), parent -> {
          if (parent instanceof ParameterizedFeatureNode) {
            FeatureNode featureNode = (FeatureNode) parent;
            int iterationIndex = Integer.parseInt(lastSegment.getValue());
            // TODO Add iterationIndex as allowed index to featureNode
          }
          return Optional.empty();
        })
        .map(node -> toResult(node).withPerfectMatch(false));
    }
    return Optional.empty();
  }

  private Optional<SpockNode> toNode(SpecNode specNode, Predicate<FeatureInfo> predicate) {
    return specNode.getSpecInfo().getAllFeatures().stream()
      .filter(predicate)
      .filter(feature -> !feature.isExcluded())
      .findAny()
      .map(feature -> createNode(specNode, feature));
  }

  private SpockNode createNode(SpecNode specNode, FeatureInfo feature) {
    if (feature.isParameterized()) {
      return describeParameterizedFeature(specNode.getUniqueId(), feature);
    } else {
      return describeSimpleFeature(specNode.getUniqueId(), feature);
    }
  }

  private FeatureNode describeParameterizedFeature(UniqueId parentId, FeatureInfo feature) {
    return new ParameterizedFeatureNode(toUniqueId(parentId, feature), feature);
  }

  private SpockNode describeSimpleFeature(UniqueId parentId, FeatureInfo feature) {
    IterationInfo iterationInfo = new IterationInfo(feature, EMPTY_ARGS, 1);
    iterationInfo.setName(feature.getName());
    UniqueId uniqueId = toUniqueId(parentId, feature);
    IterationNode iterationNode = new IterationNode(toUniqueId(uniqueId, feature), iterationInfo);
    return new SimpleFeatureNode(uniqueId, feature, iterationNode);
  }

  private UniqueId toUniqueId(UniqueId parentId, FeatureInfo feature) {
    return parentId.append("feature", feature.getFeatureMethod().getReflection().getName());
  }

  private Result toResult(TestDescriptor node) {
    return Result.of(Match.of(node, () -> {
      // TODO allow all iteration indexes
      return emptySet();
    }));
  }

}
