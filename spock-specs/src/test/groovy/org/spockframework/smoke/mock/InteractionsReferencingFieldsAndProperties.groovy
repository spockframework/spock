/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.smoke.mock

import spock.lang.Specification

class InteractionsReferencingFieldsAndProperties extends Specification {
  private aField = 1
  def aProperty = "abc"

  def "interactions can reference fields"() {
    def oracle = Mock(Oracle)

    when:
    def result = oracle.askQuestion(aField)

    then:
    oracle.askQuestion(aField) >> aField
    result == aField
  }

  def "interactions can reference properties"() {
    def oracle = Mock(Oracle)

    when:
    def result = oracle.askQuestion(aProperty)

    then:
    oracle.askQuestion(aProperty) >> aProperty
    result == aProperty
  }
}

interface Oracle {
   def askQuestion(q)
}
