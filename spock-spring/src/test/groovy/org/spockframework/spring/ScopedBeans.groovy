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

import spock.lang.Specification
import spock.util.EmbeddedSpecRunner

import org.springframework.beans.factory.BeanCreationException

class ScopedBeans extends Specification {


  def "scoped beans are not scanned by default"() {
    def runner = new EmbeddedSpecRunner()

    when:
    def result = runner.run '''
package org.spockframework.spring

import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(locations = "ScopedBeansThrowing-context.xml")
class SpecWithScope extends Specification {

  def "no exception"() {
    expect:
    true
  }
}
'''

    then:
    noExceptionThrown()
    result.testsSucceededCount == 1
    result.testsFailedCount == 0
  }


  def "all scoped beans are scanned if annotated"() {
    def runner = new EmbeddedSpecRunner()

    when:
    runner.run '''
package org.spockframework.spring

import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ScanScopedBeans
@ContextConfiguration(locations = "ScopedBeansThrowing-context.xml")
class SpecWithScopes extends Specification {

  def "this will fail"() {
    expect:
    true
  }
}
'''

    then:
    BeanCreationException e = thrown()
    e.message == 'ExpectedThrow'
  }

  def "scoped beans are scanned if annotated and mocks attached"() {
    def runner = new EmbeddedSpecRunner()

    when:
    def result = runner.run('''
package org.spockframework.spring

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ScanScopedBeans
@ContextConfiguration(locations = "ScopedBeansMock-context.xml")
class SpecWithScopes extends Specification {

  @Autowired
  IService2 service

  def "this will not fail"() {
    when:
    service.generateQuickBrownFox()

    then:
    1 * service.generateQuickBrownFox()
  }
}
''')

    then:
    noExceptionThrown()
    result.testsSucceededCount == 1
    result.testsFailedCount == 0

  }


  def "  scopes to scan can be defined"(){
    def runner = new EmbeddedSpecRunner()

    when:
    def result = runner.run('''
package org.spockframework.spring

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ScanScopedBeans('session')
@ContextConfiguration(locations = "ScopedBeansThrowing-context.xml")
class SpecWithScopes extends Specification {

  @Autowired
  IService2 service

  def "this will not fail"() {
    when:
    service.generateQuickBrownFox()

    then:
    1 * service.generateQuickBrownFox()
  }
}
''')

    then:
    noExceptionThrown()
    result.testsSucceededCount == 1
    result.testsFailedCount == 0

  }
}

