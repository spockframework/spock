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

package org.spockframework.spring.mock;

import java.util.List;

import org.springframework.test.context.*;

/**
 * A {@link ContextCustomizerFactory} to add Mockito support.
 *
 * original author Phillip Webb
 */
class SpockContextCustomizerFactory implements ContextCustomizerFactory {

  @Override
  public ContextCustomizer createContextCustomizer(Class<?> testClass,
                                                   List<ContextConfigurationAttributes> configAttributes) {
    // We gather the explicit mock definitions here since they form part of the
    // MergedContextConfiguration key. Different mocks need to have a different key.
    DefinitionsParser parser = new DefinitionsParser();
    parser.parse(testClass);
    return new SpockContextCustomizer(parser.getDefinitions());
  }

}
