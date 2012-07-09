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

import org.spockframework.util.CollectionUtil;
import org.spockframework.util.InternalSpockError;
import spock.lang.Specification;
import spock.mock.MockConfiguration;
import spock.mock.MockImplementation;

import java.util.Map;

public class CompositeMockFactory implements IMockFactory {
  public static CompositeMockFactory INSTANCE =
      new CompositeMockFactory(CollectionUtil.mapOf(MockImplementation.JAVA,
          JavaMockFactory.INSTANCE, MockImplementation.GROOVY, GroovyMockFactory.INSTANCE));

  private final Map<MockImplementation, IMockFactory> mockFactories;

  public CompositeMockFactory(Map<MockImplementation, IMockFactory> mockFactories) {
    this.mockFactories = mockFactories;
  }

  public Object create(MockConfiguration configuration, Specification specification) {
    IMockFactory factory = mockFactories.get(configuration.getImplementation());
    if (factory == null) {
      throw new InternalSpockError("No mock factory for implementation '%s' registered").withArgs(configuration.getImplementation());
    }
    return factory.create(configuration, specification);
  }
}
