/*
 * Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.spring.mock


import org.spockframework.spring.Service1
import org.spockframework.spring.Service2
import org.spockframework.spring.SpringSpy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import spock.lang.Issue
import spock.lang.Specification

@Issue("https://github.com/spockframework/spock/issues/1790")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ContextConfiguration(classes = SpyMockContext)
class SpringSpyDirtiesContextSpec extends Specification {

  @SpringSpy
  Service2 service2

  @Autowired
  Service1 service1

  def "default implementation is used"() {
    expect:
    service1.generateString() == "The quick brown fox jumps over the lazy dog."
  }

  def "mocking works was well"() {
    when:
    def result = service1.generateString()

    then:
    result == "Foo"
    1 * service2.generateQuickBrownFox() >> "Foo"
  }
}
