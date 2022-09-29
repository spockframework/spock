/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.junit4;

import org.spockframework.runtime.model.*;

import java.util.*;

// This extension supports different JUnit versions with different rule capabilities/implementations.
// Implementation makes use of reflection and nested classes to make sure that no ClassNotFoundErrorS will occur.
@SuppressWarnings("UnusedDeclaration")
public class RuleExtension extends AbstractRuleExtension {
  @Override
  public void visitSpec(SpecInfo spec) {
    if (ruleClass == null) return;

    List<FieldInfo> methodRuleFields = new ArrayList<>();
    List<FieldInfo> testRuleFields = new ArrayList<>();

    for (FieldInfo field : spec.getAllFields()) {
      if (!field.isAnnotationPresent(ruleClass)) continue;
      checkIsInstanceField(field);

      if (hasFieldType(field, methodRuleClass)) {
        methodRuleFields.add(field);
      } else if (hasFieldType(field, testRuleClass)) {
        testRuleFields.add(field);
      } else {
        invalidFieldType(field);
      }
    }

    if (!methodRuleFields.isEmpty()) MethodRuleInterceptorInstaller.install(spec, methodRuleFields);
    if (!testRuleFields.isEmpty()) TestRuleInterceptorInstaller.install(spec, testRuleFields);
  }

  private static class MethodRuleInterceptorInstaller {
    static void install(SpecInfo spec, List<FieldInfo> ruleFields) {
      MethodRuleInterceptor interceptor = new MethodRuleInterceptor(ruleFields);
      for (FeatureInfo feature : spec.getAllFeatures()) {
        feature.addIterationInterceptor(interceptor);
      }
    }
  }

  private static class TestRuleInterceptorInstaller {
    static void install(SpecInfo spec, List<FieldInfo> ruleFields) {
      TestRuleInterceptor interceptor = new TestRuleInterceptor(ruleFields, spec);
      for (FeatureInfo feature : spec.getAllFeatures()) {
        feature.addIterationInterceptor(interceptor);
      }
    }
  }
}
