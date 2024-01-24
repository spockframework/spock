/*
 * Copyright 2024 the original author or authors.
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

package org.spockframework.smoke.mock

import org.spockframework.EmbeddedSpecification
import org.spockframework.mock.runtime.GroovyMockMetaClass
import org.spockframework.runtime.GroovyRuntimeUtil

class GroovyMockFinalClass extends EmbeddedSpecification {

  def "Mock for final class shall only change meta class of instance"() {
    given:
    def originalMetaClass = GroovyRuntimeUtil.getMetaClass(FinalClass)
    FinalClass mock = GroovyMock()
    def afterMetaClass = GroovyRuntimeUtil.getMetaClass(FinalClass)

    expect:
    !(originalMetaClass instanceof GroovyMockMetaClass)

    when:
    def result = mock.method()

    then:
    1 * mock.method() >> "MockedName"
    result == "MockedName"
    
    expect:
    mock.getMetaClass() instanceof GroovyMockMetaClass
    mock.getMetaClass() == afterMetaClass
    afterMetaClass == originalMetaClass
  }

  static final class FinalClass {
    String method() { return "Name" }
  }
}

