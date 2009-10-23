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

import javax.annotation.Resource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.util.EmbeddedSpeckRunner

@ContextConfiguration(locations = "appcontext.xml")
class InjectionExamples extends Specification {
  @Autowired
  IService service

  @Resource
  IService myService

  @Autowired
  ApplicationContext context

  def "injecting a field by type"() {
    expect:
    service instanceof Service
  }

  def "injecting a field by name"() {
    expect:
    myService instanceof Service
  }

  def "direct usage of application context (discouraged)"() {
    expect:
    context != null
    context.getBean("myService") instanceof Service
  }

  def "shared fields cannot be injected"() {
    def runner = new EmbeddedSpeckRunner()

    when:
    runner.runWithImports """
import org.spockframework.spring.IService
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration
class Foo extends Specification {
  $ann
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
    ann << ["@org.springframework.beans.factory.annotation.Autowired",
            "@javax.annotation.Resource"]
  }
}
