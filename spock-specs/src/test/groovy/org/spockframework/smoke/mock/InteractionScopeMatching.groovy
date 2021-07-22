/*
 * Copyright 2021 the original author or authors.
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

package org.spockframework.smoke.mock

import spock.lang.Specification

/**
 *
 * @author Kamil Jędrzejuk
 */
class InteractionScopeMatching extends Specification {
  List defaultMockBehaviour = Mock()
  List withOverrideLastResponse = Mock([useLastMatchResponseStrategy:true])

  def setup() {
    defaultMockBehaviour.size() >> 1
    withOverrideLastResponse.size() >> 1
  }

  def "interactions should use response matching algorithm depends on passed useLastMatchResponseStrategy flag when determining the stubbed reply"() {
    given:
    defaultMockBehaviour.size() >> 2
    withOverrideLastResponse.size() >> 2

    and:
    withOverrideLastResponse.size() >> 3

    expect:
    defaultMockBehaviour.size() == 1
    withOverrideLastResponse.size() == 3
  }
}
