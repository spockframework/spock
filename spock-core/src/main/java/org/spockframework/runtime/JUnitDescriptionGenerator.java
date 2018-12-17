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

import java.lang.annotation.Annotation;

import org.junit.runner.Description;

import org.spockframework.runtime.model.*;

/**
 * Generates and attaches JUnit Description's to a SpecInfo's nodes.
 *
 * @author Peter Niederwieser
 */
@SuppressWarnings("ALL")
public class JUnitDescriptionGenerator {
  private final SpecInfo spec;

  public JUnitDescriptionGenerator(SpecInfo spec) {
    this.spec = spec;
  }

  public void describeSpecMethods() {
    Description desc = Description.createSuiteDescription(spec.getReflection());
    spec.setDescription(desc);

    for (FeatureInfo feature : spec.getAllFeatures())
      describeFeature(feature);

    for (MethodInfo fixtureMethod : spec.getAllFixtureMethods())
      describeMethod(fixtureMethod);
  }

  public void describeSpec() {
    Description desc = Description.createSuiteDescription(spec.getReflection());
    spec.setDescription(desc);

    // important for JUnit compatibility: Descriptions of specs that are reported
    // as "ignored" to JUnit must not have any children
    if (spec.isExcluded() || spec.isSkipped()) return;

    for (FeatureInfo feature : spec.getAllFeaturesInExecutionOrder()) {
      if (feature.isExcluded()) continue;
      if (feature.isReportIterations()) continue; // don't report up-front because IDEs don't handle this well
      desc.addChild(feature.getFeatureMethod().getDescription());
    }
  }

  private void describeFeature(FeatureInfo feature) {
    Description desc = describeMethod(feature.getFeatureMethod());
    feature.setDescription(desc);
    if (feature.getDataProcessorMethod() != null)
      feature.getDataProcessorMethod().setDescription(desc);
    for (DataProviderInfo prov : feature.getDataProviders())
      prov.getDataProviderMethod().setDescription(desc);
  }

  private Description describeMethod(MethodInfo method) {
    Annotation[] anns = method.getReflection() == null ?
        new Annotation[0] : method.getAnnotations();
    Description desc = Description.createTestDescription(spec.getReflection(),
        method.getName(), anns);
    method.setDescription(desc);
    return desc;
  }
}
