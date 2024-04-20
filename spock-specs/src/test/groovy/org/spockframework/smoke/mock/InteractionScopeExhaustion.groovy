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

import org.spockframework.mock.IMockInteraction
import org.spockframework.mock.runtime.InteractionScope
import org.spockframework.mock.runtime.MockInvocation
import org.spockframework.runtime.RunContext
import spock.config.RunnerConfiguration
import spock.lang.Specification

/**
 *
 * @author Bouke Nijhuis
 */
class InteractionScopeExhaustion extends Specification {

  def "exhausted interactions should be skipped (if possible) for the firstMatch algorithm"() {
    expect:
    test(match1, isExhausted1, match2, isExhausted2, resultText)

    where:
    match1 | isExhausted1 || match2 | isExhausted2 || resultText
    //false  | false        || false  | false      ||
    //false  | false        || false  | true       ||
    false  | false        || true   | false        || "2"
    false  | false        || true   | true         || "2"

    //false  | true        || false  | false       ||
    //false  | true        || false  | true        ||
    false  | true         || true   | false        || "2"
    false  | true         || true   | true         || "2"

    true   | false        || false  | false        || "1"
    true   | false        || false  | true         || "1"
    true   | false        || true   | false        || "1"
    true   | false        || true   | true         || "1"

    true   | true         || false  | false        || "1"
    true   | true         || false  | true         || "1"
    true   | true         || true   | false        || "2"
    true   | true         || true   | true         || "1"
  }

  def "exhausted interactions should be skipped (if possible) for the lastMatch algorithm"() {
    RunContext.get().getConfiguration(RunnerConfiguration.class).matchFirstInteraction = false

    expect:
    test(match1, isExhausted1, match2, isExhausted2, resultText)

    cleanup:
    RunContext.get().getConfiguration(RunnerConfiguration.class).matchFirstInteraction = true

    where:
    match1 | isExhausted1 || match2 | isExhausted2 || resultText
    //false  | false        || false  | false      ||
    //false  | false        || false  | true       ||
    false  | false        || true   | false        || "2"
    false  | false        || true   | true         || "2"

    //false  | true        || false  | false       ||
    //false  | true        || false  | true        ||
    false  | true         || true   | false        || "2"
    false  | true         || true   | true         || "2"

    true   | false        || false  | false        || "1"
    true   | false        || false  | true         || "1"
    true   | false        || true   | false        || "2"
    true   | false        || true   | true         || "1"

    true   | true         || false  | false        || "1"
    true   | true         || false  | true         || "1"
    true   | true         || true   | false        || "2"
    true   | true         || true   | true         || "1"
  }


  def test(boolean match1, boolean isExhausted1, boolean match2, boolean isExhausted2, String resultText) {
    InteractionScope interactionScope = new InteractionScope()
    MockInvocation mockInvocation = Mock()

    IMockInteraction mockInteraction1 = Mock()
    interactionScope.addInteraction(mockInteraction1)
    mockInteraction1.matches(mockInvocation) >> match1
    mockInteraction1.isExhausted() >> isExhausted1
    mockInteraction1.getText() >> "1"

    IMockInteraction mockInteraction2 = Mock()
    interactionScope.addInteraction(mockInteraction2)
    mockInteraction2.matches(mockInvocation) >> match2
    mockInteraction2.isExhausted() >> isExhausted2
    mockInteraction2.getText() >> "2"

    IMockInteraction mockInteractionResult = interactionScope.match(mockInvocation)

    return mockInteractionResult.getText() == resultText
  }

}
