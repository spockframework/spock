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

package org.spockframework.smoke.mock

import org.spockframework.mock.CannotCreateMockException
import org.spockframework.runtime.model.parallel.Resources
import spock.lang.ResourceLock
import spock.lang.Specification
import spock.lang.Unroll

class GroovyMocks extends Specification {
  def "implement GroovyObject"() {
    expect:
    GroovyMock(List) instanceof GroovyObject
    GroovyMock(ArrayList) instanceof GroovyObject
  }

  @ResourceLock(
    value = Resources.META_CLASS_REGISTRY,
    reason = "Global Mocks"
  )
  @Unroll("A Groovy#typeB can't be created for a type that was already Groovy#typeA'd")
  def "global GroovyMocks can't be created for a type that is already mocked"(String typeA, String typeB) {
    given:
    createMock(typeA)

    when:
    createMock(typeB)

    then:
    CannotCreateMockException e = thrown()
    e.message == 'Cannot create mock for class org.spockframework.smoke.mock.GroovyMocks$LocalClassForMocking. The given type is already mocked by Spock.'

    where:
    [typeA, typeB] << ([['Mock', 'Stub', 'Spy']] * 2).combinations()
  }

  void createMock(String type) {
    switch (type) {
      case 'Mock':
        GroovyMock(global: true, LocalClassForMocking)
        break
      case 'Stub':
        GroovyStub(global: true, LocalClassForMocking)
        break
      case 'Spy':
        GroovySpy(global: true, LocalClassForMocking)
        break
      default:
        throw new IllegalArgumentException(type)
    }
  }

  class LocalClassForMocking {}
}
