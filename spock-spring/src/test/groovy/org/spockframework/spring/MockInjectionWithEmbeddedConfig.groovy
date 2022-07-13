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

package org.spockframework.spring

import org.spockframework.mock.MockUtil
import spock.lang.Specification
import spock.mock.DetachedMockFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.*
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.AnnotationConfigContextLoader

@ContextConfiguration(loader=AnnotationConfigContextLoader)
class MockInjectionWithEmbeddedConfig extends Specification {

  @Configuration
  static class Config {
    private DetachedMockFactory factory = new DetachedMockFactory()

    @Bean
    IService1 service1() {
        return factory.Mock(IService1, name: "service1")
    }

    @Bean
    IService2 service2() {
        return factory.Mock(IService2, name: "service2")
    }
  }

  @Autowired
  IService1 service1

  @Autowired
  IService2 service2

  List normalMock = Mock()

  def "Injected services are mocks"() {
    expect:
    new MockUtil().isMock(service1)
    new MockUtil().isMock(service2)
  }

  def "Mocks can be configured"() {
    when:
    assert service1.generateString() == "I can be configured"
    assert service2.generateQuickBrownFox() == "The quick brown fox..."

    then:
    1 * service1.generateString() >> "I can be configured"
    1 * service2.generateQuickBrownFox() >> "The quick brown fox..."
  }

  def "Unconfigured mocks return default"() {
    expect:
    service1.generateString() == null
    service2.generateQuickBrownFox() == null
  }

  def "normal mocks work too"() {
    when:
    def result = normalMock.get(0)

    then:
    result == "hello"
    1 * normalMock.get(_) >> 'hello'
  }
}
