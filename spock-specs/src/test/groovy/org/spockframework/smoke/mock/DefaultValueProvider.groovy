/*
 *  Copyright 2024 the original author or authors.
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
 */

package org.spockframework.smoke.mock

import spock.lang.Requires
import spock.lang.Specification

import org.spockframework.mock.FinalClass

class DefaultValueProvider extends Specification {

  @Requires(
      value = { jvm.isJava17Compatible() },
      reason = "Sealed interfaces are only supported in Java 17+"
  )
  def "Stub's EmptyOrDummyResponse does not support sealed interfaces by default"() {
    given:
    def holder = Stub(StubHolder)

    when:
    holder.getEither()

    then:
    IllegalArgumentException e = thrown()
    e.message == 'org.spockframework.smoke.mock.IEither is a sealed interface'
  }

  def "Stub's EmptyOrDummyResponse can be extended via IDefaultValueProviderExtension"() {
    given:
    def holder = Stub(StubHolder)

    when:
    def maybe = holder.getMaybe()

    then:
    maybe instanceof IMaybe.None
  }

  def "Stub's EmptyOrDummyResponse can be extended via IDefaultValueProviderExtension for final classes"() {
    given:
    def holder = Stub(StubHolder)

    when:
    def finalClass = holder.getFinal()

    then:
    finalClass instanceof FinalClass
    finalClass.value == "default"
  }

  interface StubHolder {
    IMaybe<String> getMaybe()

    IEither<String, Exception> getEither()

    FinalClass getFinal()
  }
}
