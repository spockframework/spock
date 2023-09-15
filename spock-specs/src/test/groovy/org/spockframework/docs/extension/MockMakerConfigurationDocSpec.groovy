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

package org.spockframework.docs.extension

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.RunContext
import spock.mock.MockMakers

class MockMakerConfigurationDocSpec extends EmbeddedSpecification {

  @SuppressWarnings('UnnecessaryQualifiedReference')
  def "preferredMockMaker configuration setting"() {
    given:
    runner.configurationScript {
      // tag::mock-maker-preferredMockMaker[]
      mockMaker {
        preferredMockMaker spock.mock.MockMakers.byteBuddy
      }
      // end::mock-maker-preferredMockMaker[]
    }
    runner.addClassImport(RunContext)
    runner.addClassImport(MockMakers)
    runner.runFeatureBody("""
    def registry = RunContext.get().getMockMakerRegistry()
    expect:
    registry.getMakerList().get(0).getId().toString() == "${MockMakers.byteBuddy}"
""")
  }
}
