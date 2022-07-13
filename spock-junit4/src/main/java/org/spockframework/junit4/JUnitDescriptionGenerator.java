/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.junit4;

import org.spockframework.runtime.model.*;

import java.lang.annotation.Annotation;

import org.junit.runner.Description;

/**
 * Generates and attaches JUnit Description's to a SpecInfo's nodes.
 *
 * @author Peter Niederwieser
 */
@SuppressWarnings("ALL")
public class JUnitDescriptionGenerator {

  public static Description describeSpec(SpecInfo spec) {
    Description desc = Description.createSuiteDescription(spec.getReflection());

    // important for JUnit compatibility: Descriptions of specs that are reported
    // as "ignored" to JUnit must not have any children
    if (spec.isExcluded() || spec.isSkipped()) {
      return desc;
    }

    for (FeatureInfo feature : spec.getAllFeaturesInExecutionOrder()) {
      if (feature.isExcluded()) {
        continue;
      }
      desc.addChild(describeFeature(feature, spec));
    }
    return desc;
  }

  public static Description describeFeature(FeatureInfo feature, SpecInfo spec) {
    return describeMethod(feature.getFeatureMethod(), spec);
  }

  public static Description describeIteration(IterationInfo iteration, SpecInfo spec) {
    return Description.createTestDescription(
      spec.getReflection(),
      iteration.getDisplayName(),
      iteration.getFeature().getFeatureMethod().getAnnotations());
  }

  private static Description describeMethod(MethodInfo method, SpecInfo spec) {
    Annotation[] anns = method.getReflection() == null ?
      new Annotation[0] : method.getAnnotations();
    Description desc = Description.createTestDescription(spec.getReflection(),
      method.getName(), anns);
    return desc;
  }
}
