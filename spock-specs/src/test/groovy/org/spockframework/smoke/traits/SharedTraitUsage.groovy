/*
* Copyright 2014 the original author or authors.
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
package org.spockframework.smoke.traits

import spock.lang.Ignore
import spock.lang.Issue
import spock.lang.Specification

class SharedTraitUsage extends Specification implements MySharedTrait {

  @Ignore
  @Issue("https://code.google.com/p/spock/issues/detail?id=370")
  def "should not restore inline assigned value on shared field defined in trait before every test"() {
    expect:
      sharedValue == "sharedValueInitializedInSetupSpec"
  }

  @Ignore
  @Issue("https://code.google.com/p/spock/issues/detail?id=370")
  def "should not restore not initialized shared field defined in trait before every test"() {
    expect:
      notInitializedSharedValue == "notInitializedSharedValueInitializedInSetupSpec"
  }

  def "should not restore inline assigned value on instance field defined in trait before every test"() {
    expect:
      instanceValue == "instanceValueInitializedInSetup"
  }

  def "should not restore not initialized instance field defined in trait before every test"() {
    expect:
      notInitializedInstanceValue == "notInitializedInstanceValueInitializedInSetup"
  }
}
