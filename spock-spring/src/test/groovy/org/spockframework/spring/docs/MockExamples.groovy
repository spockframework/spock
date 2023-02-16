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

package org.spockframework.spring.docs

import spock.lang.IgnoreIf
import spock.lang.Specification

import javax.inject.Named

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration

abstract class MockExampleBase extends Specification {

//tag::example[]
  @Autowired @Named('serviceMock')
  GreeterService serviceMock

  @Autowired @Named('serviceStub')
  GreeterService serviceStub

  @Autowired @Named('serviceSpy')
  GreeterService serviceSpy

  @Autowired @Named('alternativeMock')
  GreeterService alternativeMock

  def "mock service"() {
    when:
    def result = serviceMock.greeting

    then:
    result == 'mock me'
    1 * serviceMock.getGreeting() >> 'mock me'
  }

  def "sub service"() {
    given:
    serviceStub.getGreeting() >> 'stub me'

    expect:
    serviceStub.greeting == 'stub me'
  }

  def "spy service"() {
    when:
    def result = serviceSpy.greeting

    then:
    result == 'Hello World'
    1 * serviceSpy.getGreeting()
  }

  def "alternative mock service"() {
    when:
    def result = alternativeMock.greeting

    then:
    result == 'mock me'
    1 * alternativeMock.getGreeting() >> 'mock me'
  }
//end::example[]
}


@ContextConfiguration(classes = DetachedJavaConfig)
class DetachedJavaConfigExample extends MockExampleBase {
}


@IgnoreIf(
  value = { jvm.java17Compatible },
  reason = "Spring 4s reflection doesn't work on 17+"
)
@ContextConfiguration(locations = "classpath:org/spockframework/spring/docs/MockDocu-context.xml")
class DetachedXmlExample extends MockExampleBase {
}
