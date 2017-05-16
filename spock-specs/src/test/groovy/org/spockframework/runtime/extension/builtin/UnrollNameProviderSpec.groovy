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

import org.spockframework.runtime.model.FeatureInfo

import spock.lang.*

class UnrollNameProviderSpec extends Specification {
  @Issue("http://issues.spockframework.org/detail?id=115")
  def "regex-like data values are substituted correctly (i.e. literally)"() {
    def feature = new FeatureInfo()
    feature.addParameterName("dataVar")
    def nameGenerator = new UnrollNameProvider(feature, "foo #dataVar bar")

    expect:
    nameGenerator.nameFor(Collections.singletonMap("dataVar", value)) == name

    where:
    value                   | name
    '$'                     | 'foo $ bar'
    /#([a-zA-Z_\$][\w\$]*)/ | /foo #([a-zA-Z_\$][\w\$]*) bar/
  }

  def "data values are converted to strings in Groovy style"() {
    def feature = new FeatureInfo()
    feature.addParameterName("dataVar")
    def nameGenerator = new UnrollNameProvider(feature, "foo #dataVar bar")

    expect:
    nameGenerator.nameFor(Collections.singletonMap("dataVar", value)) == name

    where:
    value                   | name
    [1, 2, 3]               | 'foo [1, 2, 3] bar'
    [a: 1, b: 2]            | 'foo [a:1, b:2] bar'
  }
}
