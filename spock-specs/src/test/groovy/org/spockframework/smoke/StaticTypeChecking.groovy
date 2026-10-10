/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.smoke

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.ConditionNotSatisfiedError

import spock.lang.Issue
import spock.lang.Specification

class StaticTypeChecking extends EmbeddedSpecification {

  @Issue("https://github.com/spockframework/spock/issues/2343")
  def "preserves receiver types in conditions"(String visibility, String condition) {
    when:
    runner.runWithImports("""
      @groovy.transform.TypeChecked
      class Example extends Specification {
        $visibility String myField

        def feature() {
          $condition
        }
      }
    """)

    then:
    noExceptionThrown()

    where:
    [visibility, condition] << [["", "public"], ["""
      when:
      this.myField = 'value'

      then:
      this.myField == 'value'
    """, """
      expect:
      this.myField == null
    """, """
      when:
      this.myField = 'value'
      assert this.myField == 'value'

      then:
      true
    """]].combinations()
  }

  @Issue("https://github.com/spockframework/spock/issues/2343")
  def "records values in failing type checked conditions"(String value) {
    when:
    runner.runWithImports("""
      @groovy.transform.TypeChecked
      class Example extends Specification {
        String myField = $value

        def feature() {
          expect:
          this.myField != $value
        }
      }
    """)

    then:
    def error = thrown(ConditionNotSatisfiedError)
    def values = error.condition.values
    values[0] instanceof Specification
    values[1] == values[0].myField
    values[2..3] == [values[1], false]

    where:
    value << ["'value'", "null"]
  }

  def "correctly spelled setup compiles successfully"() {
    when:
    compiler.compileSpecBody("""
@groovy.transform.TypeChecked
def foo(Object element){
  assert element != null
}""")

    then:
    noExceptionThrown()
  }
}
