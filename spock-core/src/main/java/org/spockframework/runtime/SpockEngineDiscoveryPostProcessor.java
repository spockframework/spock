package org.spockframework.runtime;

import org.spockframework.runtime.model.*;

import org.junit.platform.engine.*;

class SpockEngineDiscoveryPostProcessor {

  private static final Object[] EMPTY_ARGS = new Object[0];

  SpockEngineDescriptor postProcessEngineDescriptor(UniqueId uniqueId, RunContext runContext,
    SpockEngineDescriptor engineDescriptor) {
    SpockEngineDescriptor processedEngineDescriptor = new SpockEngineDescriptor(uniqueId, runContext);
    engineDescriptor.getChildren().stream()
      .map(child -> processSpecNode(child, runContext))
      .forEach(processedEngineDescriptor::addChild);
    return processedEngineDescriptor;
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
    IterationInfo iterationInfo = new IterationInfo(feature, 0, EMPTY_ARGS, 1);
    iterationInfo.setName(feature.getName());
    UniqueId uniqueId = toUniqueId(parentId, feature);
    IterationNode iterationNode = new IterationNode(toUniqueId(uniqueId, feature), iterationInfo);
    return new SimpleFeatureNode(uniqueId, feature, iterationNode);
  }

  private UniqueId toUniqueId(UniqueId parentId, FeatureInfo feature) {
    return parentId.append("feature", feature.getFeatureMethod().getReflection().getName());
  }

  private TestDescriptor processSpecNode(TestDescriptor child, RunContext runContext) {
    if (child instanceof SpecNode) {
      SpecNode specNode = (SpecNode) child;
      try {
        runContext.createExtensionRunner(specNode.getSpecInfo()).run();
      } catch (Exception e) {
        return new ErrorSpecNode(specNode.getUniqueId(), specNode.getSpecInfo(), e);
      }
      specNode.getSpecInfo().getAllFeaturesInExecutionOrder().stream()
        .filter(featureInfo -> !featureInfo.isExcluded())
        .map(featureInfo -> createNode(specNode, featureInfo))
        .forEach(specNode::addChild);
    }
    return child;
  }
}
