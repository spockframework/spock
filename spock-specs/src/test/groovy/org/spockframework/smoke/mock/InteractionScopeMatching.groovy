/*
 * Copyright 2020 the original author or authors.
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

import org.spockframework.runtime.RunContext
import spock.config.RunnerConfiguration
import spock.lang.Specification

/**
 *
 * @author Bouke Nijhuis
 */
class InteractionScopeMatching extends Specification {
  List list = Mock()

  def setup() {
    list.size() >> 1
    list.size() >> -1
  }

  def "RunnerConfiguration.matchFirstInteraction should default to true"() {
    expect:
    RunContext.get().getConfiguration(RunnerConfiguration.class).matchFirstInteraction
  }

  def "interactions should (by default) use the first match algorithm when determining the stubbed reply"() {
    expect: // it to use the first defined reply of the method
    list.size() == 1
  }

  def "interactions should (when specified) use the last match algorithm when determining the stubbed reply"() {
    RunContext.get().getConfiguration(RunnerConfiguration.class).matchFirstInteraction = false

    expect: // it to use the last defined reply of the method
    list.size() == -1

    when: // adding an interaction outside of the setup method
    list.size() >> -2

    then: // I expect it the use the last defined reply of the method
    list.size() == -2

    cleanup:
    RunContext.get().getConfiguration(RunnerConfiguration.class).matchFirstInteraction = true
  }
}
