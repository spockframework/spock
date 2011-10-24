/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.extension.builtin;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;

import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.extension.IGlobalExtension;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.model.*;
import org.spockframework.util.ReflectionUtil;

public class RuleExtension implements IGlobalExtension {
  private static boolean ruleClassAvailable = ReflectionUtil.isClassAvailable("org.junit.Rule");
  private static boolean methodRuleClassAvailable = ReflectionUtil.isClassAvailable("org.junit.rules.MethodRule");
  private static boolean testRuleClassAvailable = ReflectionUtil.isClassAvailable("org.junit.rules.TestRule");

  public void visitSpec(SpecInfo spec) {
    if (!ruleClassAvailable) return;

    List<FieldInfo> ruleFields = RuleCollector.collectFields(spec);
    if (ruleFields.isEmpty()) return;

    if (methodRuleClassAvailable) {
      IMethodInterceptor interceptor = MethodRuleInterceptorFactory.create(ruleFields);
      for (FeatureInfo feature : spec.getAllFeatures())
        feature.getFeatureMethod().addInterceptor(interceptor);
    }

    if (testRuleClassAvailable) {
      IMethodInterceptor interceptor = TestRuleInterceptorFactory.create(ruleFields);
      for (FeatureInfo feature : spec.getAllFeatures())
        feature.addIterationInterceptor(interceptor);
    }
  }

  private static class RuleCollector {
    static List<FieldInfo> collectFields(SpecInfo spec) {
      List<FieldInfo> fields = new ArrayList<FieldInfo>();
      for (FieldInfo field : spec.getAllFields())
        if (field.getReflection().isAnnotationPresent(Rule.class)) {
          checkIsInstanceField(field);
          fields.add(field);
        }
      return fields;
    }

    private static void checkIsInstanceField(FieldInfo field) {
      if (field.isShared() || field.isStatic()) {
        throw new InvalidSpecException("@Rule field '%s' has to be an instance field").withArgs(field.getName());
      }
    }
  }

  // defer loading of class MethodRule
  private static class MethodRuleInterceptorFactory {
    static IMethodInterceptor create(List<FieldInfo> ruleFields) {
      return new MethodRuleInterceptor(ruleFields);
    }
  }

  // defer loading of class TestRule
  private static class TestRuleInterceptorFactory {
    static IMethodInterceptor create(List<FieldInfo> ruleFields) {
      return new TestRuleInterceptor(ruleFields);
    }
  }
}
