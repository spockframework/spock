/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.boot2

import org.spockframework.boot2.service.HelloWorldService
import org.spockframework.spring.ScanScopedBeans
import spock.lang.Specification
import spock.mock.DetachedMockFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.*
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.*
import org.springframework.web.context.annotation.RequestScope

/**
 * Tests enabled scanning of scoped and proxied beans.
 */
@ScanScopedBeans
@SpringBootTest(properties = ['spring.main.allow-bean-definition-overriding=true'])
class SpringBootTestAnnotationScopedProxyMockSpec extends Specification {
  @Autowired
  ApplicationContext context

  @Autowired
  HelloWorldService helloWorldService

  def "test context loads"() {
    expect:
    context != null
    context.containsBean("helloWorldService")
    context.containsBean("scopedTarget.helloWorldService")
    context.containsBean("simpleBootApp")
  }

  def "scoped mock can be used"() {
    given:
    def helloWorldServiceMock = context.getBean("scopedTarget.helloWorldService") as HelloWorldService

    expect:
    helloWorldService.class.simpleName.startsWith('HelloWorldService$$EnhancerBySpringCGLIB$$')

    when:
    def result = helloWorldService.helloMessage

    then:
    1 * helloWorldServiceMock.getHelloMessage() >> 'sup?'
    result == 'sup?'
  }


  @TestConfiguration
  static class MockConfig {
    def detachedMockFactory = new DetachedMockFactory()

    @Bean @RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
    HelloWorldService helloWorldService() {
      return detachedMockFactory.Mock(HelloWorldService)
    }
  }
}
