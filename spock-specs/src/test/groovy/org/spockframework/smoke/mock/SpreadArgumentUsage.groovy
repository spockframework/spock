/*
 * Copyright 2026 the original author or authors.
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

import spock.lang.Specification

class SpreadArgumentUsage extends Specification {
  def "matches runtime spread arguments in interaction declarations"() {
    InternalClass mock = Mock()

    when:
    mock."$methodName"(*args)

    then:
    1 * mock."$methodName"(*args)

    where:
    methodName           | args
    "noArgs"             | []
    "oneArg"             | ["oneArg"]
    "objectArg"           | [null]
    "objectArg"           | [["arrayArg"] as Object[]]
    "firstMethod"        | ["firstArg", "secondArg"]
    "secondMethod"       | ["firstArg", "secondArg", 123L]
    "varargsOnly"        | ["firstVararg", "secondVararg"]
    "fixedThenVarargs"   | ["fixedArg", "firstVararg", "secondVararg"]
  }
}

class InternalClass {
  void noArgs() {}
  void oneArg(String arg) {}
  void objectArg(Object arg) {}
  void firstMethod(String firstArg, String secondArg) {}
  void secondMethod(String firstArg, String secondArg, long thirdArg) {}
  void varargsOnly(String... args) {}
  void fixedThenVarargs(String fixedArg, String... args) {}
}
