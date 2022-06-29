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
import org.springframework.context.annotation.*
import org.springframework.scheduling.annotation.*
import org.springframework.test.context.ContextConfiguration

/**
 * Tests for a mock bean where the mocked interface has an async method.
 *
 * original author Andy Wilkinson
 * @author Leonard Brünings
 */
@ContextConfiguration
class SpringBeanWithAsyncInterfaceMethodIntegrationSpec extends Specification {

  @SpringBean
  private Transformer transformer = Mock()

  @Autowired
  private MyService service

  def 'mocked methods are not async'() {
    given:
    transformer.transform('foo') >> 'bar'

    expect:
    service.transform('foo') == 'bar'
  }


  static class MyService {

    private final Transformer transformer

    MyService(Transformer transformer) {
      this.transformer = transformer
    }

    String transform(String input) {
      return transformer.transform(input)
    }

  }

  @Configuration
  @EnableAsync
  static class MyConfiguration {

    @Bean
    MyService myService(Transformer transformer) {
      return new MyService(transformer)
    }

  }

}

interface Transformer {

  @Async
  String transform(String input)

}
