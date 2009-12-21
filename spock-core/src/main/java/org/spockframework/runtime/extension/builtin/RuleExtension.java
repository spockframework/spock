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

import org.spockframework.runtime.extension.IGlobalExtension;
import org.spockframework.runtime.model.*;

public class RuleExtension implements IGlobalExtension {
  public void visitSpec(SpecInfo spec) {
    List<FieldInfo> ruleFields = new ArrayList<FieldInfo>();
    for (FieldInfo field : spec.getFields())
      if (field.getReflection().isAnnotationPresent(Rule.class))
        ruleFields.add(field);

    if (ruleFields.isEmpty()) return;

    RuleInterceptor interceptor = new RuleInterceptor(ruleFields);
    for (FeatureInfo feature : spec.getFeatures())
      feature.getFeatureMethod().addInterceptor(interceptor);
  } 
}
