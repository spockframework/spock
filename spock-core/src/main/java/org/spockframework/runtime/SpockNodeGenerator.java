/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime;

import org.junit.platform.engine.UniqueId;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;

/**
 * Generates and attaches JUnit Description's to a SpecInfo's nodes.
 *
 * @author Peter Niederwieser
 */
@SuppressWarnings("ALL")
class SpockNodeGenerator {
  private final UniqueId baseId;
  private final RunContext runContext;

  SpockNodeGenerator(UniqueId baseId, RunContext runContext) {
    this.baseId = baseId;
    this.runContext = runContext;
  }

  SpecNode describeSpec(Class<?> specClass) {
    SpecInfo specInfo = new SpecInfoBuilder(specClass).build();
    SpecNode specNode = new SpecNode(baseId.append("spec", specInfo.getReflection().getName()), specInfo);
    runContext.createExtensionRunner(specInfo).run();

    for (FeatureInfo feature : specInfo.getAllFeaturesInExecutionOrder()) {
      if (feature.isExcluded()) continue;

      specNode.addChild(describeFeature(specNode.getUniqueId(), feature));
    }

    return specNode;
  }

  private FeatureNode describeFeature(UniqueId parentId, FeatureInfo feature) {
    return new FeatureNode(parentId.append("feature", feature.getFeatureMethod().getName()), feature);
  }
}
