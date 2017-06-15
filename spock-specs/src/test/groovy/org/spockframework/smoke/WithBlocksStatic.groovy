/*
 * Copyright 2012 the original author or authors.
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
package org.spockframework.smoke

import groovy.transform.CompileStatic
import spock.lang.Specification

@CompileStatic
class WithBlocksStatic extends Specification {

  def "can statically compile against type of the target"() {
    def fred = new Person(spouse: new Person(name: "Wilma"))

    expect:
    with(fred.spouse) {
      name == "Wilma"
      age == 42
    }
  }

  static class Person {
    String name = "Fred"
    int age = 42
    Person spouse
  }
}
