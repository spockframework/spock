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

import org.springframework.core.ResolvableType;
import org.springframework.util.ObjectUtils;

abstract class FieldDefinition implements Definition {
  static final int MULTIPLIER = 31;

  private final FieldInfo fieldInfo;
  private final QualifierDefinition qualifier;

  protected final ResolvableType resolvableType;

  FieldDefinition(FieldInfo fieldInfo) {
    this.fieldInfo = fieldInfo;
    resolvableType = ResolvableType.forField(fieldInfo.getReflection());
    qualifier = QualifierDefinition.forElement(fieldInfo.getReflection());
  }

  FieldInfo getFieldInfo() {
    return fieldInfo;
  }

  public QualifierDefinition getQualifier() {
    return qualifier;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = MULTIPLIER * result + ObjectUtils.nullSafeHashCode(this.getName());
    result = MULTIPLIER * result + ObjectUtils.nullSafeHashCode(this.qualifier);
    result = MULTIPLIER * result + ObjectUtils.nullSafeHashCode(this.resolvableType);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
      return false;
    }
    FieldDefinition other = (FieldDefinition) obj;
    boolean result;
    result = ObjectUtils.nullSafeEquals(this.getName(), other.getName());
    result = result && ObjectUtils.nullSafeEquals(this.qualifier, other.qualifier);
    result = result && ObjectUtils.nullSafeEquals(this.resolvableType, other.resolvableType);
    return result;
  }
}
