/*
 * Copyright 2011 the original author or authors.
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
import spock.lang.Unroll

class SpreadArgumentUsage extends Specification {
  @Unroll
  def "matches runtime spread arguments for #methodName"() {
    InternalClass internalClass = Mock()
    WrapperClass wrapper = new WrapperClass(internalClass: internalClass)

    when:
    wrapper."$methodName"(*args)

    then:
    1 * internalClass."$methodName"(*args)

    where:
    methodName     | args
    "noArgs"       | []
    "firstMethod"  | ["firstArg", "secondArg"]
    "secondMethod" | ["firstArg", "secondArg", 123L]
  }
}

interface InternalClass {
  void noArgs()
  void firstMethod(String firstArg, String secondArg)
  void secondMethod(String firstArg, String secondArg, long thirdArg)
}

class WrapperClass {
  InternalClass internalClass

  void noArgs() {
    internalClass.noArgs()
  }

  void firstMethod(String firstArg, String secondArg) {
    internalClass.firstMethod(firstArg, secondArg)
  }

  void secondMethod(String firstArg, String secondArg, long thirdArg) {
    internalClass.secondMethod(firstArg, secondArg, thirdArg)
  }
}
