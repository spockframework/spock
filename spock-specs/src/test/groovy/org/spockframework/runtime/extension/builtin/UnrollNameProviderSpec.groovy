/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.extension.builtin

import org.spockframework.runtime.SpockAssertionError
import org.spockframework.runtime.model.FeatureInfo
import spock.lang.*
import spock.util.environment.RestoreSystemProperties

class UnrollNameProviderSpec extends Specification {
  @Issue("https://github.com/spockframework/spock/issues/237")
  def "regex-like data values are substituted correctly (i.e. literally)"() {
    given:
    def feature = new FeatureInfo()
    feature.addDataVariable("dataVar")
    def nameGenerator = new UnrollNameProvider(feature, "foo #dataVar bar")

    expect:
    nameGenerator.nameFor(value) == name

    where:
    value                   | name
    '$'                     | 'foo $ bar'
    /#([a-zA-Z_\$][\w\$]*)/ | /foo #([a-zA-Z_\$][\w\$]*) bar/
  }

  def "data values are converted to strings in Groovy style"() {
    given:
    def feature = new FeatureInfo()
    feature.addDataVariable("dataVar")
    def nameGenerator = new UnrollNameProvider(feature, "foo #dataVar bar")

    expect:
    nameGenerator.nameFor(value) == name

    where:
    value                   | name
    [1, 2, 3]               | 'foo [1, 2, 3] bar'
    [a: 1, b: 2]            | 'foo [a:1, b:2] bar'
  }

  def "missing variables are rendered as #Error:dataVars"() {
    given:
    def feature = new FeatureInfo()
    feature.addDataVariable("dataVar")
    def nameGenerator = new UnrollNameProvider(feature, "foo #dataVars bar")

    expect:
    nameGenerator.nameFor('1') == "foo #Error:dataVars bar"
  }

  def "exceptions during variable eval are rendered as #Error:dataVars"() {
    given:
    def feature = new FeatureInfo()
    feature.addDataVariable("dataVar")
    def nameGenerator = new UnrollNameProvider(feature, "foo #dataVar.foo bar")

    expect:
    nameGenerator.nameFor('1') == "foo #Error:dataVar.foo bar"
  }

  @Issue("https://github.com/spockframework/spock/issues/767")
  @RestoreSystemProperties
  def "missing variables throw an exception if spock.throwUnrollExceptions is set to true"() {
    given:
    System.setProperty('spock.assertUnrollExpressions', 'true')
    def feature = new FeatureInfo()
    feature.addDataVariable("dataVar")
    def nameGenerator = new UnrollNameProvider(feature, "foo #dataVars bar")

    when:
    nameGenerator.nameFor('1') == "foo #Error:dataVars bar"

    then:
    def e = thrown(SpockAssertionError)
    e.message == 'Error in @Unroll, could not find matching variable for expression: dataVars'
    e.cause == null
  }

  @Issue("https://github.com/spockframework/spock/issues/767")
  @RestoreSystemProperties
  def "exceptions during variable eval throw an exception if spock.throwUnrollExceptions is set to true"() {
    given:
    def feature = new FeatureInfo()
    System.setProperty('spock.assertUnrollExpressions', 'true')
    feature.addDataVariable("dataVar")
    def nameGenerator = new UnrollNameProvider(feature, "foo #dataVar.foo bar")

    when:
    nameGenerator.nameFor('1') == "foo #Error:dataVar.foo bar"

    then:
    def e = thrown(SpockAssertionError)
    e.message == 'Error in @Unroll expression: dataVar.foo'
    e.cause.message == 'No such property: foo for class: java.lang.String'
  }
}
