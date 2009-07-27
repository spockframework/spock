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

import org.junit.runner.Description;

import org.spockframework.runtime.model.SpeckInfo;
import org.spockframework.runtime.model.MethodInfo;

/**
 * Generates and attaches JUnit Description's to a SpeckInfo's nodes.
 *
 * @author Peter Niederwieser
 */
public class JUnitMetadataGenerator {
  private final SpeckInfo speck;

  public JUnitMetadataGenerator(SpeckInfo speck) {
    this.speck = speck;
  }

  public void generate() {
    Description desc = Description.createSuiteDescription(speck.getName());
    speck.setMetadata(desc);
    for (MethodInfo feature : speck.getFeatureMethods())
      desc.addChild(describeMethod(feature));

    describeMethod(speck.getSetupMethod());
    describeMethod(speck.getCleanupMethod());
    describeMethod(speck.getSetupSpeckMethod());
    describeMethod(speck.getCleanupSpeckMethod());
  }

  private Description describeMethod(MethodInfo method) {
    Description desc = Description.createTestDescription(speck.getReflection(), method.getName());

    method.setMetadata(desc);
    if (method.getDataProcessor() != null)
      method.getDataProcessor().setMetadata(desc);
    for (MethodInfo prov : method.getDataProviders())
      prov.setMetadata(desc);

    return desc;
  }
}
