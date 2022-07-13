/*
 * Copyright 2012 the original author or authors.
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

import java.lang.annotation.Annotation;

import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.extension.*;
import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.util.ReflectionUtil;
import org.spockframework.util.Nullable;

public abstract class AbstractRuleExtension implements IGlobalExtension {
  @SuppressWarnings("unchecked")
  protected static Class<? extends Annotation> ruleClass = (Class) ReflectionUtil.loadClassIfAvailable("org.junit.Rule");

  @SuppressWarnings("unchecked")
  protected static Class<? extends Annotation> classRuleClass = (Class) ReflectionUtil.loadClassIfAvailable("org.junit.ClassRule");

  @Nullable
  protected static Class<?> methodRuleClass = ReflectionUtil.loadClassIfAvailable("org.junit.rules.MethodRule");

  @Nullable
  protected static Class<?> testRuleClass = ReflectionUtil.loadClassIfAvailable("org.junit.rules.TestRule");

  protected void checkIsInstanceField(FieldInfo field) {
    if (field.isShared() || field.isStatic()) {
      throw new InvalidSpecException("@Rule fields cannot be @Shared. Either do not make '%s' @Shared, or use @ClassRule.").withArgs(field.getName());
    }
  }

  protected void checkIsSharedField(FieldInfo field) {
    if (!field.isShared()) {
      throw new InvalidSpecException("@ClassRule fields must be @Shared. Either make '%s' @Shared, or use @Rule.").withArgs(field.getName());
    }
  }

  protected boolean hasFieldType(FieldInfo field, @Nullable Class<?> ruleClass) {
    return ruleClass != null && ruleClass.isAssignableFrom(field.getType());
  }

  protected void invalidFieldType(FieldInfo field) {
    if (field.getType() == Object.class) {
      throw new InvalidSpecException("@Rule field '%s' does not have a declared type. Please add a type declaration.").withArgs(field.getName());
    }
    throw new InvalidSpecException("The declared type of @Rule field '%s' does not appear to be a rule type.").withArgs(field.getName());
  }
}
