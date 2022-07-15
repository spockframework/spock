/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.runtime.extension.builtin

import org.spockframework.runtime.SpockAssertionError
import org.spockframework.runtime.model.IterationInfo
import spock.lang.*

class UnrollIterationNameProviderSpec extends Specification {
  @Issue("https://github.com/spockframework/spock/issues/237")
  def "regex-like data values are substituted correctly (i.e. literally)"() {
    given:
    def nameGenerator = new UnrollIterationNameProvider(null, "foo #dataVar bar", false)
    IterationInfo iterationInfo = Stub {
      it.iterationIndex >> 0
      it.dataVariables >> [dataVar: value]
    }

    expect:
    nameGenerator.getName(iterationInfo) == name

    where:
    value                   | name
    '$'                     | 'foo $ bar'
    /#([a-zA-Z_\$][\w\$]*)/ | /foo #([a-zA-Z_\$][\w\$]*) bar/
  }

  def "data values are converted to strings in Groovy style"() {
    given:
    def nameGenerator = new UnrollIterationNameProvider(null, "foo #dataVar bar", false)
    IterationInfo iterationInfo = Stub {
      it.iterationIndex >> 0
      it.dataVariables >> [dataVar: value]
    }

    expect:
    nameGenerator.getName(iterationInfo) == name

    where:
    value                   | name
    [1, 2, 3]               | 'foo [1, 2, 3] bar'
    [a: 1, b: 2]            | 'foo [a:1, b:2] bar'
  }

  def "missing variables are rendered as #Error:dataVars"() {
    given:
    def nameGenerator = new UnrollIterationNameProvider(null, "foo #dataVars bar", false)
    IterationInfo iterationInfo = Stub {
      it.iterationIndex >> 0
      it.dataVariables >> [dataVar: '1']
    }

    expect:
    nameGenerator.getName(iterationInfo) == "foo #Error:dataVars bar"
  }

  def "exceptions during variable eval are rendered as #Error:dataVars"() {
    given:
    def nameGenerator = new UnrollIterationNameProvider(null, "foo #dataVar.foo bar", false)
    IterationInfo iterationInfo = Stub {
      it.iterationIndex >> 0
      it.dataVariables >> [dataVar: '1']
    }

    expect:
    nameGenerator.getName(iterationInfo) == "foo #Error:dataVar.foo bar"
  }

  @Issue("https://github.com/spockframework/spock/issues/767")
  def "missing variables throw an exception if validateExpressions is set to true"() {
    given:
    def nameGenerator = new UnrollIterationNameProvider(null, "foo #dataVars bar", true)

    when:
    nameGenerator.getName(Stub(IterationInfo)) == "foo #Error:dataVars bar"

    then:
    def e = thrown(SpockAssertionError)
    e.message == 'Error in @Unroll, could not find matching variable for expression: dataVars'
    e.cause == null
  }

  @Issue("https://github.com/spockframework/spock/issues/767")
  def "exceptions during variable eval throw an exception if validateExpressions is set to true"() {
    given:
    def nameGenerator = new UnrollIterationNameProvider(null, "foo #dataVar.foo bar", true)
    IterationInfo iterationInfo = Stub {
      it.iterationIndex >> 0
      it.dataVariables >> [dataVar: '1']
    }

    when:
    nameGenerator.getName(iterationInfo) == "foo #Error:dataVar.foo bar"

    then:
    def e = thrown(SpockAssertionError)
    e.message == 'Error in @Unroll expression: dataVar.foo'
    e.cause.message == 'No such property: foo for class: java.lang.String'
  }
}
