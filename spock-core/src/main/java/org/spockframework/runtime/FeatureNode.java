/*
 * Copyright 2023 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

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
  public void nodeSkipped(SpockExecutionContext context, TestDescriptor testDescriptor, SkipResult result) {
    context.getRunner().supervisor.featureSkipped(getNodeInfo());
  }

  @Override
  public SpockExecutionContext prepare(SpockExecutionContext context) throws Exception {
    return context.withChildStoreProvider();

  }

  @Override
  public void around(SpockExecutionContext context, Invocation<SpockExecutionContext> invocation) {
    context.getRunContext().ensureInstalled();
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
