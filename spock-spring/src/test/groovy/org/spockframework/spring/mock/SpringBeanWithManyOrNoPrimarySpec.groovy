/*
 * Copyright 2022 the original author or authors.
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

import spock.lang.Specification
import spock.util.EmbeddedSpecRunner

class SpringBeanWithManyOrNoPrimarySpec extends Specification {

  def "rejects an attempt to mock a bean with more than one instance without primary"() {
    setup:
    def runner = new EmbeddedSpecRunner()
    runner.throwFailure = false

    when:
    def result = runner.run("""
import org.spockframework.spring.Service2
import org.spockframework.spring.SpringBean
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = TwoService2BeansWithMultiplePrimaryConfig)
class Foo extends Specification {

  @SpringBean
  Service2 service2 = Mock()

  def foo() {
    expect:
    1==1
  }

  static class TwoService2BeansWithMultiplePrimaryConfig {

    @Bean
    Service2 service2Primary1() {
      new Service2()
    }

    @Bean
    Service2 service2Primary2() {
      new Service2()
    }
  }
}
""")

    then:
    result.totalFailureCount > 0
    result.failures[0].exception.message.contains("Failed to load ApplicationContext")
  }

  def "rejects an attempt to mock a bean with more than one primary instance"() {
    setup:
    def runner = new EmbeddedSpecRunner()
    runner.throwFailure = false

    when:
    def result = runner.run("""
import org.spockframework.spring.Service2
import org.spockframework.spring.SpringBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = TwoService2BeansWithMultiplePrimaryConfig)
class Foo extends Specification {

  @SpringBean
  Service2 service2 = Mock()

  def foo() {
    expect:
    1==1
  }

  static class TwoService2BeansWithMultiplePrimaryConfig {

    @Primary
    @Bean
    Service2 service2Primary1() {
      new Service2()
    }

    @Primary
    @Bean
    Service2 service2Primary2() {
      new Service2()
    }
  }
}
""")

    then:
    result.totalFailureCount > 0
    result.failures[0].exception.message.contains("Failed to load ApplicationContext")
  }

}
