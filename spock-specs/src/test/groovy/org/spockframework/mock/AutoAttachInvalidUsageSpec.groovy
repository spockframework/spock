/*
 * Copyright 2017 the original author or authors.
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
 *
 */

package org.spockframework.mock

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.SpockException
import spock.mock.AutoAttach

class AutoAttachInvalidUsageSpec extends EmbeddedSpecification {

  def "setup"() {
    runner.addClassImport(AutoAttach)
  }


  def "not on shared"() {
    when:
    runner.runSpecBody('''

    @AutoAttach
    @Shared
    List mockShared

    def "foo"() {
    expect:
    mockShared.get(1) == 0
    }
''')

    then:
    def ex = thrown(SpockException)
    ex.message == '@AutoAttach is only supported for instance fields (offending field: apackage.ASpec.mockShared)'
  }

  def "null field"() {
    when:
    runner.runWithImports("""
      @Stepwise
      class NullFieldSpec extends Specification {
        @AutoAttach
        def field = null

        def "feature"()  { expect: true }
      }
    """)
    then:
    SpockException ex = thrown()
    ex.message == "Cannot AutoAttach 'null' for field field:3"
  }

  def "No mock value for field"() {
    when:
    runner.runWithImports("""
      @Stepwise
      class NoMockSpec extends Specification {
        @AutoAttach
        def field = "Value"

        def "feature"()  { expect: true }
      }
    """)
    then:
    SpockException ex = thrown()
    ex.message == "AutoAttach failed 'Value' is not a mock for field field:3"
  }
}
