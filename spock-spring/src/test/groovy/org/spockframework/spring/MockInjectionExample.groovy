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

import javax.inject.Named

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(locations = "MockExamples-context.xml")
class MockInjectionExample extends Specification {

  @Autowired @Named('serviceMock')
  IService1 serviceMock


  @Autowired @Named('serviceStub')
  IService1 serviceStub

  @Autowired @Named('serviceSpy')
  IService2 serviceSpy

  @Autowired @Named('service2')
  IService2 service2

  @Autowired @Named('nonMock')
  ArrayList concreteSpy;

  def "Injected services are mocks"() {
    expect:
    new MockUtil().isMock(serviceMock)
    new MockUtil().isMock(serviceStub)
    new MockUtil().isMock(serviceSpy)
    new MockUtil().isMock(service2)
  }

  def "Mocks can be configured"() {
    given:
    serviceStub.generateString() >> "I can be configured"

    when:
    assert serviceMock.generateString() == "I can be configured"
    assert serviceStub.generateString() == "I can be configured"
    assert serviceSpy.generateQuickBrownFox() == "I can be configured"
    assert service2.generateQuickBrownFox() == "The quick brown fox..."

    then:
    1 * serviceMock.generateString() >> "I can be configured"
    1 * serviceSpy.generateQuickBrownFox() >> "I can be configured"
    1 * service2.generateQuickBrownFox() >> "The quick brown fox..."
  }

  def "Unconfigured mocks return default"() {
    expect:
    serviceMock.generateString() == null
    serviceStub.generateString() == ""
    serviceSpy.generateQuickBrownFox() == "The quick brown fox jumps over the lazy dog."
    service2.generateQuickBrownFox() == null

  }

  def "Spies can be created from concrete objects and injected"() {
    when:
    concreteSpy.size()

    then:
    1 * concreteSpy.size() >> 0
  }

}
