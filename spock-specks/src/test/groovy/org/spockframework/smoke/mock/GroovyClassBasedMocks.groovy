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

package org.spockframework.smoke.mock

import org.junit.runner.RunWith
import org.spockframework.dsl.*
import static org.spockframework.dsl.Predef.*
import org.spockframework.mock.InteractionNotSatisfiedError

/**
 * A ...
 *
 * @author Peter Niederwieser
 */
@Speck
@RunWith(Sputnik)
class GroovyClassBasedMocks {
  MockMe mockMe = Mock()

  def "mock declared method"() {
    when:
    mockMe.foo(42)
    then:
    1 * mockMe.foo(42)
  }

  @FailsWith(value=InteractionNotSatisfiedError,reason="TODO")
  def "mock GDK method"() {
    when:
    mockMe.any()
    then:
    1 * mockMe.any()
  }

  @FailsWith(value=MissingMethodException,reason="TODO")
  def "mock dynamic method"() {
    when:
    mockMe.someMethod()
    then:
    1 * mockMe.someMethod()
  }
}

class MockMe {
  def foo(int i) {}
}