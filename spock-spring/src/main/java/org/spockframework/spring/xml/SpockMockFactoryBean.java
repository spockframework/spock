/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.spring.xml;

import org.spockframework.mock.MockNature;
import org.springframework.beans.factory.FactoryBean;
import spock.mock.DetachedMockFactory;

import java.util.Locale;

import static java.util.Collections.emptyMap;

/**
 * Takes care of instantiating detached spock Mocks.
 *
 * Spring integration of spock mocks is heavily inspired by
 * Springokito {@see https://bitbucket.org/kubek2k/springockito}.
 *
 * @author Leonard Bruenings
 */
public class SpockMockFactoryBean<T> implements FactoryBean<T> {

  private final Class<T> targetClass;
  private String name;
  private String mockNature = MockNature.MOCK.name();

  private T instance;

  public SpockMockFactoryBean (Class<T> targetClass) {
    this.targetClass = targetClass;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T getObject() throws Exception {
    if (instance == null) {
      MockNature nature = MockNature.valueOf(mockNature.toUpperCase(Locale.ROOT));
      instance = new DetachedMockFactory().createMock(name, targetClass, nature, emptyMap());
    }
    return instance;
  }

  @Override
  public Class<?> getObjectType() {
    return targetClass;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMockNature() {
    return mockNature;
  }

  public void setMockNature(String mockNature) {
    this.mockNature = mockNature;
  }
}
