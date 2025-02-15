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

import org.spockframework.mock.runtime.MockCreationSettings;
import org.spockframework.runtime.RunContext;
import org.spockframework.runtime.model.FieldInfo;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

abstract class SpockSpringProxyCreator {

  static Object create(FieldInfo fieldInfo) {
    DelegatingInterceptor delegatingInterceptor = new DelegatingInterceptor(fieldInfo);
    MockCreationSettings settings = MockCreationSettings.settings(
      fieldInfo.getType(),
      emptyList(),
      delegatingInterceptor,
      SpockSpringProxyCreator.class.getClassLoader(),
      true);
    Object proxy = RunContext.get().getMockMakerRegistry().makeMock(settings);
    return proxy;
  }
}
