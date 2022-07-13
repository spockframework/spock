/*
 * Copyright 2018 the original author or authors.
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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.*
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(classes = SpringConfig)
class LazyBeans extends Specification {
  @Autowired
  Object foo

  def "foo#bar shouldn't be initialized"() {
    expect:
    foo == "bar"
  }
}

@Configuration
class SpringConfig {
  @Bean
  Object foo() {
    "bar"
  }

  @Lazy
  @Bean
  List bar() {
    assert false: "@Lazy beans should not be initialized."
  }
}
