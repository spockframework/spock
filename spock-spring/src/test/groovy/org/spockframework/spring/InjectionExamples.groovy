/*
 * Copyright 2009 the original author or authors.
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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration

import spock.util.EmbeddedSpecRunner

import spock.lang.*
import org.spockframework.util.Util

@ContextConfiguration(locations = "InjectionExamples-context.xml")
class InjectionExamples extends Specification {
  @Autowired
  IService service

  @Autowired
  ApplicationContext context

  def "injecting a field by type"() {
    expect:
    service instanceof Service
  }

  def "injecting a field by name (@Resource is JDK 1.6 only)"() {
    if (!Util.isClassAvailable("javax.annotation.Resource")) return

    def runner = new EmbeddedSpecRunner()
    
    when:
    runner.run """
package org.spockframework.spring

import javax.annotation.Resource
import org.spockframework.spring.IService
import org.springframework.test.context.ContextConfiguration
import spock.lang.*

@ContextConfiguration(locations = "InjectionExamples-context.xml")
class Foo extends Specification {
  @Resource
  IService myService

  def foo() {
    expect:
    myService instanceof IService
  }
}
    """

    then:
    noExceptionThrown()
  }

  def "direct usage of application context (discouraged)"() {
    expect:
    context != null
    context.getBean("myService") instanceof Service
  }

  def "shared fields cannot be injected"() {
    if (!Util.isClassAvailable(ann)) return

    def runner = new EmbeddedSpecRunner()

    when:
    runner.runWithImports """
import org.spockframework.spring.IService
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration
class Foo extends Specification {
  @$ann
  @Shared
  IService sharedService

  def foo() {
    expect: true
  }
}
    """

    then:
    thrown(SpringExtensionException)

    where:
    ann << ["org.springframework.beans.factory.annotation.Autowired",
            "javax.annotation.Resource"]
  }
}
