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

import org.spockframework.spring.SpringSpy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = CircularBeansContext)
class SpringSpyForCircularBeansSpec extends Specification {

  @SpringSpy
  private One one

  @Autowired
  private Two two

  def "bean with circular dependencies can be spied"() {
    when:
    two.callOne()
    then:
    1 * one.someMethod()
  }

  @Configuration
  @Import([One, Two])
  static class CircularBeansContext {

  }

  static class One {

    @Autowired
    private Two two

    void someMethod() {
    }

  }

  static class Two {

    @Autowired
    private One one

    void callOne() {
      one.someMethod();
    }
  }
}
