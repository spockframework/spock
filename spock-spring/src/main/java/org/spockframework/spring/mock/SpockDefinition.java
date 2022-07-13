/*
 * Copyright 2017 the original author or authors.
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

package org.spockframework.spring.mock;

import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.spring.*;
import org.spockframework.util.Assert;

import java.util.*;

import org.springframework.core.ResolvableType;
import org.springframework.util.ObjectUtils;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

class SpockDefinition extends FieldDefinition implements MockDefinition {

  private final SpringBean annotation;

  SpockDefinition(FieldInfo fieldInfo) {
    super(fieldInfo);
    validate();
    annotation = fieldInfo.getAnnotation(SpringBean.class);
  }

  private void validate() {
    FieldInfo fieldInfo = getFieldInfo();
    Assert.that(fieldInfo.isAnnotationPresent(SpringBean.class),
      "SpringBean annotation is required for this field: '%s.%s:%d' ",
      fieldInfo.getParent().getName(), fieldInfo.getName(), fieldInfo.getLine());
    if (!fieldInfo.hasInitializer()) {
      throw new SpringExtensionException(String.format("Field '%s.%s:%d' needs to have an initializer, e.g. List l = Mock()",
        fieldInfo.getParent().getName(), fieldInfo.getName(), fieldInfo.getLine()));
    }
    if (Object.class.equals(fieldInfo.getType())) {
      throw new SpringExtensionException(String.format("Field '%s.%s:%d' must use a concrete type, not def or Object.",
        fieldInfo.getParent().getName(), fieldInfo.getName(), fieldInfo.getLine()));
    }
  }

  @Override
  public String getName() {
    return annotation.name();
  }

  @Override
  public List<String> getAliases() {
    if (annotation.aliases().length == 0) {
      return singletonList(getFieldInfo().getName());
    }
    List<String> aliases = new ArrayList<>(asList(annotation.aliases()));
    aliases.add(getFieldInfo().getName());
    return aliases;
  }

  @Override
  public ResolvableType getTypeToMock() {
    return resolvableType;
  }

  @Override
  public Object createMock(String s) {
    return SpockSpringProxyCreator.create(getFieldInfo());
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = MULTIPLIER * result + ObjectUtils.nullSafeHashCode(this.getTypeToMock());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || obj.getClass() != getClass()) {
      return false;
    }
    SpockDefinition other = (SpockDefinition) obj;
    boolean result = super.equals(obj);
    result = result && ObjectUtils.nullSafeEquals(this.getTypeToMock(), other.getTypeToMock());
    return result;
  }
}
