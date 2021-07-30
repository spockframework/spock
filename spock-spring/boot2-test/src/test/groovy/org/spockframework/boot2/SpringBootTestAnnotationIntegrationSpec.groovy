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

import spock.lang.Specification

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext

/**
 * Integration test similar to {@link SimpleBootAppIntegrationSpec} but using the {@link SpringBootTest} annotation.
 *
 * SpringBootTest.webEnvironment needs to something other than {@link SpringBootTest.WebEnvironment.MOCK},
 * otherwise there is always a MockRequest active.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
  properties = ['spring.main.allow-bean-definition-overriding=true'])
class SpringBootTestAnnotationIntegrationSpec extends Specification {
  @Autowired
  ApplicationContext context

  def "test context loads"() {
    expect:
    context != null
    context.containsBean("helloWorldService")
    context.containsBean("simpleBootApp")
    context.containsBean("scopedHelloWorldService")
  }
}
