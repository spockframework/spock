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

package org.spockframework.lang;

import org.spockframework.mock.*;
import org.spockframework.mock.runtime.*;
import org.spockframework.runtime.*;
import org.spockframework.util.*;

import java.lang.reflect.Type;
import java.util.*;

import groovy.lang.Closure;

import static org.spockframework.util.ObjectUtil.uncheckedCast;

public abstract class SpecInternals {
  private final ISpecificationContext specificationContext = new SpecificationContext();

  @Beta
  public ISpecificationContext getSpecificationContext() {
    return specificationContext;
  }

  @Beta
  public <T> T createMock(@Nullable String name, Type type, MockNature nature,
      MockImplementation implementation, Map<String, Object> options, @Nullable Closure<?> closure) {
    return createMock(name, null, type, nature, implementation, options, closure);
  }

  @Beta
  public <T> T createMock(@Nullable String name, T instance, Type type, MockNature nature,
      MockImplementation implementation, Map<String, Object> options, @Nullable Closure<?> closure) {
    Object mock = CompositeMockFactory.INSTANCE.create(
        new MockConfiguration(name, type, instance, nature, implementation, options), uncheckedCast(this));
    if (closure != null) {
      GroovyRuntimeUtil.invokeClosure(closure, mock);
    }
    return uncheckedCast(mock);
  }

  @Beta
  public void createStaticMock(Type type, MockNature nature,
                               Map<String, Object> options) {
    MockConfiguration configuration = new MockConfiguration(null, type, null, nature, MockImplementation.JAVA, options);
    JavaMockFactory.INSTANCE.createStaticMock(configuration, uncheckedCast(this));
  }
}
