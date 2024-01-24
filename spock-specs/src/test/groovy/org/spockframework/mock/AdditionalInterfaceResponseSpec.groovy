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

package org.spockframework.mock

import spock.lang.Issue
import spock.lang.Specification

import java.util.function.IntSupplier

class AdditionalInterfaceResponseSpec extends Specification {

  @Issue("https://github.com/spockframework/spock/issues/1405")
  def "Defining responses for additionalInterfaces for Groovy class"() {
    given:
    A a = Stub(additionalInterfaces: [B]) {
      it.methodFromA() >> { "MockedA" }
      it.methodFromIf() >> { "Result" }
    }

    expect:
    a instanceof A
    a.methodFromA() == "MockedA"
    a instanceof B

    and:
    B ifType = a as B
    ifType.methodFromIf() == "Result"
    a.methodFromIf() == "Result"
  }

  def "Defining responses for additionalInterfaces for Groovy class with GroovyStub"() {
    given:
    A a = GroovyStub(additionalInterfaces: [B]) {
      it.methodFromA() >> { "MockedA" }
      it.methodFromIf() >> { "Result" }
    }

    expect:
    a instanceof A
    a.methodFromA() == "MockedA"
    a instanceof B
    a.methodFromIf() == "Result"

    and:
    B ifType = (B) a
    ifType.methodFromIf() == "Result"
  }

  @Issue("https://github.com/spockframework/spock/issues/1405")
  def "Defining responses for additionalInterfaces for Groovy interface"() {
    given:
    C c = Stub(additionalInterfaces: [B]) {
      it.methodFromIfC() >> { "ResultC" }
      it.methodFromIf() >> { "ResultB" }
    }

    expect:
    c instanceof C
    (c as C).methodFromIfC() == "ResultC"
    c instanceof B

    and:
    B ifType = c as B
    ifType.methodFromIf() == "ResultB"
    c.methodFromIf() == "ResultB"
  }

  def "Defining responses for additionalInterfaces for Groovy interface with GroovyStub"() {
    given:
    C c = GroovyStub(additionalInterfaces: [B]) {
      it.methodFromIfC() >> { "ResultC" }
      it.methodFromIf() >> { "ResultB" }
    }

    expect:
    c instanceof C
    c.methodFromIfC() == "ResultC"
    c instanceof B
    c.methodFromIf() == "ResultB"

    and:
    B ifType = (B) c
    ifType.methodFromIf() == "ResultB"
  }

  @Issue("https://github.com/spockframework/spock/issues/1405")
  def "Defining responses for additionalInterfaces for Java classes"() {
    given:
    ArrayList a = Stub(additionalInterfaces: [IntSupplier]) {
      it.getAsInt() >> { 5 }
    }

    expect:
    a instanceof ArrayList
    a instanceof IntSupplier
    a.getAsInt() == 5
  }

  static class A {
    @SuppressWarnings('GrMethodMayBeStatic')
    String methodFromA() {
      return "RealA"
    }
  }

  interface B {
    String methodFromIf()
  }

  interface C {
    String methodFromIfC()
  }
}
