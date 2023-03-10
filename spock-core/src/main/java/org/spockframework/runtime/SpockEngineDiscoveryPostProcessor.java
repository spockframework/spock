package org.spockframework.runtime;

import org.spockframework.runtime.model.*;
import spock.config.RunnerConfiguration;

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

  private FeatureNode createNode(SpecNode specNode, FeatureInfo feature, RunnerConfiguration configuration) {
    FeatureNode result;
    if (feature.isParameterized()) {
      result = describeParameterizedFeature(specNode.getUniqueId(), feature, configuration);
    } else {
      result = describeSimpleFeature(specNode.getUniqueId(), feature, configuration);
    }
    if (!feature.getDependees().isEmpty() || !feature.getDependencies().isEmpty()) {
      result = new DependencyFeatureNode(result);
    }
    feature.setNode(result);
    return result;
  }

  private FeatureNode describeParameterizedFeature(UniqueId parentId, FeatureInfo feature,
                                                   RunnerConfiguration configuration) {
    return new ParameterizedFeatureNode(toUniqueId(parentId, feature), configuration, feature);
  }

  private FeatureNode describeSimpleFeature(UniqueId parentId, FeatureInfo feature, RunnerConfiguration configuration) {
    IterationInfo iterationInfo = new IterationInfo(feature, 0, EMPTY_ARGS, 1);
    iterationInfo.setName(feature.getName());
    UniqueId uniqueId = toUniqueId(parentId, feature);
    IterationNode iterationNode = new IterationNode(toUniqueId(uniqueId, feature), configuration, iterationInfo);
    return new SimpleFeatureNode(uniqueId, configuration, feature, iterationNode);
  }

  private UniqueId toUniqueId(UniqueId parentId, FeatureInfo feature) {
    return parentId.append("feature", feature.getFeatureMethod().getReflection().getName());
  }

  private TestDescriptor processSpecNode(TestDescriptor child, RunContext runContext) {
    if (child instanceof SpecNode) {
      SpecNode specNode = (SpecNode) child;
      RunnerConfiguration configuration = runContext.getConfiguration(RunnerConfiguration.class);
      SpecInfo nodeInfo = specNode.getNodeInfo();
      try {
        runContext.createExtensionRunner(nodeInfo).run();
      } catch (Exception e) {
        return new ErrorSpecNode(specNode.getUniqueId(), configuration, nodeInfo,
          e);
      }

      // We have to replace the node, as the display name could have changed, and it is fixed at construction time.
      SpecNode resultNode = new SpecNode(specNode.getUniqueId(), configuration, nodeInfo);
      nodeInfo.getAllFeaturesInExecutionOrder().stream()
        .filter(featureInfo -> !featureInfo.isExcluded())
        .map(featureInfo -> createNode(resultNode, featureInfo, configuration))
        .forEach(resultNode::addChild);
      return resultNode;
    }
    return child;
  }
}
