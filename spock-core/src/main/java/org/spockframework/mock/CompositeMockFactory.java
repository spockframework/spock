/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.mock;

import java.util.Arrays;
import java.util.List;

import org.spockframework.util.InternalSpockError;
import org.spockframework.util.UnreachableCodeError;

import spock.lang.Specification;

public class CompositeMockFactory implements IMockFactory {
  public static CompositeMockFactory INSTANCE =
      new CompositeMockFactory(Arrays.asList(JavaMockFactory.INSTANCE, GroovyMockFactory.INSTANCE));

  private final List<IMockFactory> mockFactories;

  public CompositeMockFactory(List<IMockFactory> mockFactories) {
    this.mockFactories = mockFactories;
  }

  public boolean canCreate(MockConfiguration configuration) {
    throw new UnreachableCodeError("canCreate");
  }

  public Object create(MockConfiguration configuration, Specification specification) {
    for (IMockFactory factory : mockFactories) {
      if (factory.canCreate(configuration)) {
        return factory.create(configuration, specification);
      }
    }

    throw new InternalSpockError("No matching mock factory found");
  }
}
