/*
 * Copyright 2023 the original author or authors.
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

package spock.mock

import org.codehaus.groovy.runtime.InvokerHelper
import org.spockframework.runtime.InvalidSpecException
import spock.lang.Shared
import spock.lang.Specification

class MockingApiInvalidUsageSpec extends Specification {
  @Shared
  private final Closure<?> CLOSURE = { -> }

  @Shared
  private final Map<String, Object> OPTS = [:]

  def "Invalid usage check"() {
    when:
    def api = new MockingApi()
    InvokerHelper.invokeMethod(api, methodName, args.toArray())
    then:
    InvalidSpecException ex = thrown()
    ex.message == "Mock objects can only be created inside a spec, and only during the lifetime of a feature (iteration)"

    where:
    methodName   | args
    "Mock"       | []
    "Mock"       | [Runnable]
    "Mock"       | [Runnable, CLOSURE]
    "Mock"       | [OPTS]
    "Mock"       | [OPTS, Runnable]
    "Mock"       | [OPTS, Runnable, CLOSURE]
    "Mock"       | [OPTS, CLOSURE]
    "Mock"       | [CLOSURE]

    "Stub"       | []
    "Stub"       | [Runnable]
    "Stub"       | [Runnable, CLOSURE]
    "Stub"       | [OPTS]
    "Stub"       | [OPTS, Runnable]
    "Stub"       | [OPTS, Runnable, CLOSURE]
    "Stub"       | [OPTS, CLOSURE]
    "Stub"       | [CLOSURE]

    "Spy"        | []
    "Spy"        | [Runnable]
    "Spy"        | [Runnable, CLOSURE]
    "Spy"        | [OPTS]
    "Spy"        | [OPTS, Runnable]
    "Spy"        | [OPTS, Runnable, CLOSURE]
    "Spy"        | [OPTS, CLOSURE]
    "Spy"        | [CLOSURE]
    "Spy"        | ["Obj"]
    "Spy"        | ["Obj", CLOSURE]

    "GroovyMock" | []
    "GroovyMock" | [Runnable]
    "GroovyMock" | [Runnable, CLOSURE]
    "GroovyMock" | [OPTS]
    "GroovyMock" | [OPTS, Runnable]
    "GroovyMock" | [OPTS, Runnable, CLOSURE]
    "GroovyMock" | [OPTS, CLOSURE]
    "GroovyMock" | [CLOSURE]

    "GroovyStub" | []
    "GroovyStub" | [Runnable]
    "GroovyStub" | [Runnable, CLOSURE]
    "GroovyStub" | [OPTS]
    "GroovyStub" | [OPTS, Runnable]
    "GroovyStub" | [OPTS, Runnable, CLOSURE]
    "GroovyStub" | [OPTS, CLOSURE]
    "GroovyStub" | [CLOSURE]

    "GroovySpy"  | []
    "GroovySpy"  | [Runnable]
    "GroovySpy"  | [Runnable, CLOSURE]
    "GroovySpy"  | [OPTS]
    "GroovySpy"  | [OPTS, Runnable]
    "GroovySpy"  | [OPTS, Runnable, CLOSURE]
    "GroovySpy"  | [OPTS, CLOSURE]
    "GroovySpy"  | [CLOSURE]

    "SpyStatic"  | [Runnable]
    "SpyStatic"  | [Runnable, CLOSURE]
    "SpyStatic"  | [Runnable, MockMakers.mockito]
  }
}
