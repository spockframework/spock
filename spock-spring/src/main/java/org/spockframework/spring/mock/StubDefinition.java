/*
 * Copyright 2018 the original author or authors.
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

import spock.mock.DetachedMockFactory;

import java.util.*;

import org.springframework.core.ResolvableType;
import org.springframework.util.ObjectUtils;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static org.spockframework.spring.mock.FieldDefinition.MULTIPLIER;

class StubDefinition implements MockDefinition {
  private static final DetachedMockFactory detachedMockFactory = new DetachedMockFactory();
  private final ResolvableType type;

  StubDefinition(Class<?> type) {
    this.type = ResolvableType.forClass(type);
  }

  @Override
  public Object createMock(String name) {
    return detachedMockFactory.Stub(singletonMap("name", (Object)name), type.getRawClass());
  }

  @Override
  public ResolvableType getTypeToMock() {
    return type;
  }

  @Override
  public List<String> getAliases() {
    return emptyList();
  }

  @Override
  public String getName() {
    return null; // StubBeans don't have an explicit name
  }

  @Override
  public QualifierDefinition getQualifier() {
    return null; // StubBeans don't qualifiers
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = MULTIPLIER * result + ObjectUtils.nullSafeHashCode(this.type);
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StubDefinition that = (StubDefinition)o;
    return ObjectUtils.nullSafeEquals(type, that.type);
  }
}
