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

import org.spockframework.spring.SpringSpy
import org.spockframework.spring.mock.imported.example.*
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration

/**
 * Test {@link SpringSpy} on a test class field can be used to replace existing beans when
 * the context is cached. This test is identical to
 * {@link SpringSpyOnTestFieldForExistingBeanIntegrationTests} so one of them should trigger
 * application context caching.
 *
 * original author Phillip Webb
 * @author Leonard Brünings
 * @see SpringSpyOnTestFieldForExistingBeanIntegrationTests
 */

@ContextConfiguration(classes = SpringSpyOnTestFieldForExistingBeanConfig.class)
class SpringSpyOnTestFieldForExistingBeanCacheIntegrationTests extends Specification {

  @SpringSpy
  private ExampleService exampleService

  @Autowired
  private ExampleServiceCaller caller

  void 'test spying'() throws Exception {
    when:
    def result = caller.sayGreeting()
    then:
    result == "I say simple"
    1 * exampleService.greeting()
  }

}
