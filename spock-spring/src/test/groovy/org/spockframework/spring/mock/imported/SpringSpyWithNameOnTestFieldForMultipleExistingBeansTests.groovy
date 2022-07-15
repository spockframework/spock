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

package org.spockframework.spring.mock.imported

import org.spockframework.mock.ISpockMockObject
import org.spockframework.spring.SpringSpy
import org.spockframework.spring.mock.imported.example.SimpleExampleStringGenericService
import spock.lang.Specification

import org.springframework.context.annotation.*
import org.springframework.test.context.ContextConfiguration

/**
 * Test {@link SpringSpy} on a test class field can be used to inject a spy instance when
 * there are multiple candidates and one is chosen using the name attribute.
 *
 * original authors Phillip Webb, Andy Wilkinson
 * @author Leonard Brünings
 */
@ContextConfiguration
class SpringSpyWithNameOnTestFieldForMultipleExistingBeansTests extends Specification {

  @SpringSpy(name = "two")
  private SimpleExampleStringGenericService spy

  def 'test spying'() throws Exception {
    expect:
    spy instanceof ISpockMockObject
    spy.greeting() == 'two'
  }

  @Configuration
  static class Config {

    @Bean
    SimpleExampleStringGenericService one() {
      return new SimpleExampleStringGenericService("one")
    }

    @Bean
    SimpleExampleStringGenericService two() {
      return new SimpleExampleStringGenericService("two")
    }

  }

}
