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
import spock.mock.DetachedMockFactory;

import org.springframework.core.ResolvableType;
import org.springframework.util.ObjectUtils;

class SpyDefinition extends FieldDefinition {

  private final SpringSpy annotation;

  SpyDefinition(FieldInfo fieldInfo) {
    super(fieldInfo);
    validate();
    annotation = fieldInfo.getAnnotation(SpringSpy.class);
  }

  private void validate() {
    FieldInfo fieldInfo = getFieldInfo();
    Assert.that(fieldInfo.isAnnotationPresent(SpringSpy.class),
      "SpringBean annotation is required for this field: '%s.%s:%d' ",
      fieldInfo.getParent().getName(), fieldInfo.getName(), fieldInfo.getLine());
    if (fieldInfo.hasInitializer()) {
      throw new SpringExtensionException(String.format("Field '%s.%s:%d' may not have an initializer.",
        fieldInfo.getParent().getName(), fieldInfo.getName(), fieldInfo.getLine()));
    }
    if (Object.class.equals(fieldInfo.getType())) {
      throw new SpringExtensionException(String.format("Field '%s.%s:%d' must use a concrete type, not def or Object.",
        fieldInfo.getParent().getName(), fieldInfo.getName(), fieldInfo.getLine()));
    }
  }

  ResolvableType getTypeToSpy() {
    return resolvableType;
  }

  @Override
  public String getName() {
    return annotation.name();
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = MULTIPLIER * result + ObjectUtils.nullSafeHashCode(getTypeToSpy());
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
    SpyDefinition other = (SpyDefinition) obj;
    boolean result = super.equals(obj);
    result = result && ObjectUtils.nullSafeEquals(this.getTypeToSpy(), other.getTypeToSpy());
    return result;
  }

  Object createSpy(String beanName, Object bean) {
    return new DetachedMockFactory().Spy(bean);
  }
}
