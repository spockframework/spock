/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.SpecNode
import spock.lang.Title

import static java.util.stream.Collectors.toList

@Title("A beautiful mind")
class TitleExtension extends EmbeddedSpecification {
  def "sets @Title text as spec name"() {
    expect:
    specificationContext.currentSpec.displayName == "A beautiful mind"
  }

  def "@Title changes the reported display name"() {
    when:
    def result = runner.runWithImports('''
@Title("A beautiful mind")
class ASpec extends Specification {
  def "a test"() {
    expect: true
  }
}
''')

    then:
    def containers = result.containerEvents().succeeded().filter { it.testDescriptor instanceof SpecNode }.collect(toList())
    with(containers[0].testDescriptor, SpecNode) {
      displayName == "A beautiful mind"
    }
  }
}
