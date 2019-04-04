package org.spockframework.runtime;

import org.spockframework.runtime.model.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.platform.engine.*;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;

import static java.util.Collections.*;
import static org.junit.platform.engine.discovery.DiscoverySelectors.*;

public class MethodSelectorResolver implements SelectorResolver {

  private static final Object[] EMPTY_ARGS = new Object[0];

  @Override
  public Resolution resolve(MethodSelector selector, Context context) {
    return context
      .addToParent(() -> selectClass(selector.getJavaClass()), parent -> {
        if (parent instanceof SpecNode) {
          String methodName = selector.getMethodName();
          Optional<SpockNode> node = toNode((SpecNode) parent, feature -> feature.getFeatureMethod().getReflection().getName().equals(methodName));
          if (!node.isPresent()) {
            node = toNode((SpecNode) parent, feature -> methodName.equals(feature.getName()));
          }
          return node;
        }
        return Optional.empty();
      })
      .map(node -> Resolution.match(Match.exact(node, expansionCallback(node))))
      .orElse(Resolution.unresolved());
  }

  @Override
  public Resolution resolve(UniqueIdSelector selector, Context context) {
    UniqueId uniqueId = selector.getUniqueId();
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
        .map(node -> Resolution.match(Match.exact(node, expansionCallback(node))))
        .orElse(Resolution.unresolved());
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
        .map(node -> Resolution.match(Match.partial(node, expansionCallback(node))))
        .orElse(Resolution.unresolved());
    }
    return Resolution.unresolved();
  }

  private Optional<SpockNode> toNode(SpecNode specNode, Predicate<FeatureInfo> predicate) {
    return specNode.getSpecInfo().getAllFeaturesInExecutionOrder().stream()
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

  private Supplier<Set<? extends DiscoverySelector>> expansionCallback(TestDescriptor node) {
    return () -> {
      // TODO allow all iteration indexes
      return emptySet();
    };
  }

}
