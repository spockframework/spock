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

import org.spockframework.spring.SpringBean
import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration

/**
 * Tests for a mock bean where the class being mocked uses field injection.
 *
 * original author Andy Wilkinson
 * @author Leonard Brünings
 */
@ContextConfiguration
class SpringBeanWithInjectedFieldIntegrationSpec extends Specification {

  @SpringBean
  private MyService myService = Stub()

  def 'field injection into MyService mock is not attempted'() {
    given:
    myService.getCount() >> 5

    expect:
    myService.getCount() == 5
  }

  static class MyService {

    @Autowired
    private MyRepository repository

    int getCount() {
      return repository.findAll().size()
    }

  }

}

interface MyRepository {
  List<Object> findAll()
}
