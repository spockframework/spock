/*
 * Copyright 2011 the original author or authors.
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

@SuppressWarnings("UnusedDeclaration")
public class ClassRuleExtension extends AbstractRuleExtension {
  @Override
  public void visitSpec(SpecInfo spec) {
    if (classRuleClass == null) return;

    List<FieldInfo> ruleFields = new ArrayList<>();

    for (FieldInfo field : spec.getAllFields()) {
      if (!field.isAnnotationPresent(classRuleClass)) continue;

      checkIsSharedField(field);
      if (hasFieldType(field, testRuleClass)) {
        ruleFields.add(field);
      } else {
        invalidFieldType(field);
      }
    }

    if (!ruleFields.isEmpty()) ClassRuleInterceptorInstaller.install(spec, ruleFields);
  }

  private static class ClassRuleInterceptorInstaller {
    static void install(SpecInfo spec, List<FieldInfo> ruleFields) {
      spec.addInterceptor(new ClassRuleInterceptor(ruleFields, spec));
    }
  }
}
