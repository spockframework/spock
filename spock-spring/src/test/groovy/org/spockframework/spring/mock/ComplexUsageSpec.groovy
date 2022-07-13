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

package org.spockframework.spring.mock

import org.spockframework.spring.*
import spock.lang.Specification

import javax.inject.Named
import java.lang.annotation.*

import org.springframework.beans.factory.annotation.*
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(classes = ComplexConfig)
class ComplexUsageSpec extends Specification {


  @SpringBean
  Service2 aService2 = Mock()

  @Named("myName")
  @SpringBean
  Service2 nService2 = Mock()

  @MyQualifier
  @SpringBean(aliases =  'someAlias')
  Service2 qService2 = Mock()

  @SpringBean(name = 'aName')
  Service2 sService2 = Mock()

  @Autowired
  List<Service1> service1List

  def "context loads successfully"() {
    expect: true
  }

  def "services are called correctly"() {
    when:
    service1List*.generateString()

    then:
    1 * aService2.generateQuickBrownFox()
    1 * nService2.generateQuickBrownFox()
    2 * qService2.generateQuickBrownFox()
    1 * sService2.generateQuickBrownFox()
  }
}

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE])
@interface MyQualifier {}

class ComplexConfig {
  @Bean
  Service1 service1WithAlias(Service2 aService2) {
    new Service1(aService2)
  }

  @Bean
  Service1 service1WithNamed(@Named("myName") Service2 namedService2) {
    new Service1(namedService2)
  }

  @Bean
  Service1 service1WithAnnotationQualifier(@MyQualifier Service2 annotatedService2) {
    new Service1(annotatedService2)
  }

  @Bean
  Service1 service1WithAnnotationName(Service2 aName) {
    new Service1(aName)
  }

  @Bean
  Service1 service1WithAdditionalAlias(Service2 someAlias) {
    new Service1(someAlias)
  }
}
