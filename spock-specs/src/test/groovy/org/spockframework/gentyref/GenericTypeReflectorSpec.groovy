/*
 * Copyright 2013 the original author or authors.
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

package org.spockframework.gentyref

import spock.lang.Specification

class GenericTypeReflectorSpec extends Specification {
  def "can get exact parameter types for java.lang.Object method"() {
    when:
    def types = GenericTypeReflector.getExactParameterTypes(Object.getMethod("equals", Object), List)

    then:
    notThrown(NullPointerException)
    types as List == [Object]
  }

  def "can get exact return type for java.lang.Object method"() {
    when:
    def type = GenericTypeReflector.getExactReturnType(Object.getMethod("equals", Object), List)

    then:
    notThrown(NullPointerException)
    type == boolean
  }

  public <T> T genericMethod(Class<T> arg) {
    null
  }

  def "can get exact return type for generic method"() {
    def method = getClass().getDeclaredMethod("genericMethod", Class)

    when:
    def type = GenericTypeReflector.getExactReturnType(method, getClass())

    then:
    notThrown(NullPointerException)
    type == Object
  }
}
