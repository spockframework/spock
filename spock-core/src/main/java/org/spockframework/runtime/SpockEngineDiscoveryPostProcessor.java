package org.spockframework.runtime;

import org.spockframework.runtime.model.*;
import spock.config.RunnerConfiguration;

import org.junit.platform.engine.*;

import java.util.Set;

import static java.util.Comparator.comparingInt;

class SpockEngineDiscoveryPostProcessor {

  private static final Object[] EMPTY_ARGS = new Object[0];

  SpockEngineDescriptor postProcessEngineDescriptor(UniqueId uniqueId, RunContext runContext,
    SpockEngineDescriptor engineDescriptor) {
    SpockEngineDescriptor processedEngineDescriptor = new SpockEngineDescriptor(uniqueId, runContext);
    Set<? extends TestDescriptor> testDescriptors = engineDescriptor.getChildren();
    initSpecNodes(testDescriptors, runContext);
    testDescriptors.stream()
      .map(child -> processSpecNode(child, runContext))
      .sorted(comparingInt(child -> child instanceof SpecNode ? ((SpecNode) child).getNodeInfo().getExecutionOrder() : 0))
      .forEach(processedEngineDescriptor::addChild);
    return processedEngineDescriptor;
  }

  private FeatureNode createNode(SpecNode specNode, FeatureInfo feature, RunnerConfiguration configuration) {
    if (feature.isParameterized()) {
      return describeParameterizedFeature(specNode.getUniqueId(), feature, configuration);
    } else {
      return describeSimpleFeature(specNode.getUniqueId(), feature, configuration);
    }
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

  private void initSpecNodes(Set<? extends TestDescriptor> testDescriptors, RunContext runContext) {
    runContext.createExtensionRunner().initGlobalExtensions(testDescriptors);
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
