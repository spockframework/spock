/*
 * Copyright 2022 the original author or authors.
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

package org.spockframework.spring.mock

import org.spockframework.spring.IService1
import org.spockframework.spring.Service1
import org.spockframework.spring.Service2
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = TwoService2BeansWithPrimaryConfig)
class SpringBeanWIthSinglePrimaryBeanSpec extends Specification {

  @SpringBean
  Service2 service2 = Mock() {
    generateQuickBrownFox() >> "blubb"
  }

  @Autowired
  Service1 service1

  def "injection with stubbing works"() {
    expect:
    service1.generateString() == "blubb"
  }

  def "mocking works was well"() {
    when:
    def result = service1.generateString()

    then:
    result == "Foo"
    1 * service2.generateQuickBrownFox() >> "Foo"
  }

  static class TwoService2BeansWithPrimaryConfig {

    @Bean
    Service2 service2NotPrimary() {
      new Service2()
    }

    @Primary
    @Bean
    Service2 service2Primary() {
      new Service2()
    }

    @Bean
    IService1 service1(Service2 service2) {
      return new Service1(service2)
    }
  }
}
