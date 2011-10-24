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

import org.junit.ClassRule;
import org.spockframework.runtime.extension.IGlobalExtension;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.ReflectionUtil;

import java.util.ArrayList;
import java.util.List;

public class ClassRuleExtension implements IGlobalExtension {
  private static boolean classRuleClassAvailable = ReflectionUtil.isClassAvailable("org.junit.ClassRule");

  public void visitSpec(SpecInfo spec) {
    if (!classRuleClassAvailable) return;

    List<FieldInfo> ruleFields = ClassRuleCollector.collectFields(spec);
    if (ruleFields.isEmpty()) return;

    IMethodInterceptor interceptor = TestRuleInterceptorFactory.create(ruleFields);
    spec.addInterceptor(interceptor);
  }

  // defer loading of class ClassRule
  private static class ClassRuleCollector {
    static List<FieldInfo> collectFields(SpecInfo spec) {
      List<FieldInfo> fields = new ArrayList<FieldInfo>();
      for (FieldInfo field : spec.getAllFields())
        if (field.getReflection().isAnnotationPresent(ClassRule.class))
          fields.add(field);
      return fields;
    }
  }

  // defer loading of class class TestRule
  private static class TestRuleInterceptorFactory {
    static IMethodInterceptor create(List<FieldInfo> ruleFields) {
      return new TestRuleInterceptor(ruleFields);
    }
  }
}
