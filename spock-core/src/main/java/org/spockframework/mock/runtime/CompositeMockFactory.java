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

package org.spockframework.mock.runtime;

import org.spockframework.mock.*;
import org.spockframework.util.*;
import spock.lang.Specification;

import java.util.*;

import static java.util.Arrays.asList;

public class CompositeMockFactory implements IMockFactory {
  public static final CompositeMockFactory INSTANCE =
      new CompositeMockFactory(asList(JavaMockFactory.INSTANCE, GroovyMockFactory.INSTANCE));

  private final List<IMockFactory> mockFactories;

  public CompositeMockFactory(List<IMockFactory> mockFactories) {
    this.mockFactories = mockFactories;
  }

  @Override
  public boolean canCreate(IMockConfiguration configuration) {
    throw new UnreachableCodeError("canCreate");
  }

  @Override
  public Object create(IMockConfiguration configuration, Specification specification) {
    for (IMockFactory factory : mockFactories) {
      if (factory.canCreate(configuration)) {
        return factory.create(configuration, specification);
      }
    }

    throw new InternalSpockError("No matching mock factory found");
  }

	@Override
  public Object createDetached(IMockConfiguration configuration,
                               ClassLoader classLoader) {
	  for (IMockFactory factory : mockFactories) {
      if (factory.canCreate(configuration)) {
        return factory.createDetached(configuration, classLoader);
      }
    }

    throw new InternalSpockError("No matching mock factory found");
	}
}
