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

import java.util.*;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.*;

/**
 * A {@link ContextCustomizer} to add Mockito support.
 *
 * original author Phillip Webb
 * @author Leonard Brünings
 */
class SpockContextCustomizer implements ContextCustomizer {

  private final Set<Definition> definitions;

  SpockContextCustomizer(Set<? extends Definition> definitions) {
    this.definitions = new LinkedHashSet<>(definitions);
  }

  @Override
  public void customizeContext(ConfigurableApplicationContext context,
                               MergedContextConfiguration mergedContextConfiguration) {
    if (context instanceof BeanDefinitionRegistry) {
      SpockMockPostprocessor.register((BeanDefinitionRegistry)context, this.definitions);
    }
  }

  @Override
  public int hashCode() {
    return this.definitions.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || obj.getClass() != getClass()) {
      return false;
    }
    SpockContextCustomizer other = (SpockContextCustomizer)obj;
    return this.definitions.equals(other.definitions);
  }

}
