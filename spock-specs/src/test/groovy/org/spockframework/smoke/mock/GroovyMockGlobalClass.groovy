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

import org.spockframework.runtime.model.parallel.Resources
import spock.lang.Issue
import spock.lang.ResourceLock
import spock.lang.Specification

@ResourceLock(
  value = Resources.META_CLASS_REGISTRY,
  reason = "Global Mocks"
)
@Issue('https://github.com/spockframework/spock/issues/761')
class GroovyMockGlobalClass extends Specification {

  def setup() {
    GroovySpy(ClassA, global: true)
    GroovySpy(ClassB, global: true)
  }


  def "Wildcard matching for static class methods returns correct results"() {
    given:
    def service = new Subject()
    when:
    service.myMethodUnderTest()
    then:
    (1.._) * ClassA.get(_) >> new ClassA()
    (1.._) * ClassB.get(_) >> new ClassB()
  }

}

class Subject {
  void myMethodUnderTest() {
    assert ClassA.get(1) instanceof ClassA
    assert ClassB.get(2) instanceof ClassB
  }
}

class ClassA {}

class ClassB {}
