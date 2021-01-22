/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.spring

import org.spockframework.util.ReflectionUtil
import spock.lang.*
import spock.util.EmbeddedSpecRunner

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(locations = "InjectionExamples-context.xml")
class InjectionExamples extends Specification {
  @Autowired
  IService1 service

  @Autowired
  ApplicationContext context

  def "injecting a field by type"() {
    expect:
    service instanceof IService1
  }

  @Requires({ReflectionUtil.isClassAvailable("javax.annotation.Resource")})
  def "injecting a field by name"() {
    def runner = new EmbeddedSpecRunner()

    when:
    runner.run """
package org.spockframework.spring

import javax.annotation.Resource
import org.spockframework.spring.IService1
import org.springframework.test.context.ContextConfiguration
import spock.lang.*

@ContextConfiguration(locations = "InjectionExamples-context.xml")
class Foo extends Specification {
  @Resource
  IService1 myService1

  def foo() {
    expect:
    myService1 instanceof IService1
  }
}
    """

    then:
    noExceptionThrown()
  }

  def "direct usage of application context (discouraged)"() {
    expect:
    context != null
    context.getBean("myService1") instanceof IService1
  }
}
