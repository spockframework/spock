/*
 * Copyright 2012 the original author or authors.
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

import java.util.regex.Pattern

class JavaSpyBasics extends MockBasics {
  List createListDouble() {
    createArrayListDouble()
  }

  ArrayList createArrayListDouble() {
    def list = Spy(ArrayList)
    list[42] = "foo"
    list

  }

  List createAndStubListDouble() {
    Spy(ArrayList) {
      get(42) >> "foo"
    }
  }

  List createAndMockListDouble() {
    createStubAndMockListDouble()
  }

  List createStubAndMockListDouble() {
    Spy(ArrayList) {
      1 * get(42) >> "foo"
    }
  }

  def 'can disambiguate constructors with null arguments'() {
    given:
    runner.addClassImport(Foo)
    runner.addClassImport(Pattern)

    expect:
    runner.runFeatureBody '''
given:
Foo verifier = Mock()

when:
Spy(Foo, constructorArgs: [null as String, verifier])

then:
1 * verifier.verify(String)

when:
Spy(Foo, constructorArgs: [(Pattern) null, verifier])

then:
1 * verifier.verify(Pattern)
'''
  }
}

class GroovySpyBasics extends MockBasics {
  List createListDouble() {
    createArrayListDouble()
  }

  ArrayList createArrayListDouble() {
    def list = GroovySpy(ArrayList)
    list[42] = "foo"
    list

  }

  List createAndStubListDouble() {
    GroovySpy(ArrayList) {
      get(42) >> "foo"
    }
  }

  List createAndMockListDouble() {
    createStubAndMockListDouble()
  }

  List createStubAndMockListDouble() {
    GroovySpy(ArrayList) {
      1 * get(42) >> "foo"
    }
  }

  def 'can disambiguate constructors with null arguments'() {
    given:
    Foo verifier = Mock()

    when:
    GroovySpy(Foo, constructorArgs: [null as String, verifier])

    then:
    1 * verifier.verify(String)

    when:
    GroovySpy(Foo, constructorArgs: [(Pattern) null, verifier])

    then:
    1 * verifier.verify(Pattern)
  }
}

class Foo {
  Foo(String s, def verifier) {
    verifier.verify(String)
  }

  Foo(Pattern p, def verifier) {
    verifier.verify(Pattern)
  }

  def verify(def clazz) {
  }
}
