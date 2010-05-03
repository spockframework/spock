
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

package org.spockframework.smoke.mock

import spock.lang.*

class WildcardUsages extends Specification {
  def "match any mock object"() {
    def list1 = Mock(List)
    def list2 = Mock(List)

    when:
    list1.get(1)
    list2.add("abc")

    then:
    1 * _.add("abc")
    1 * _.get(1)
  }

  def "match any mock invocation"() {

    when:
    Mock(XYY).get(1)
    Mock(XYY).get(2)
    Mock(XYY).get(3)

    then:
    3 * _._
  }
}

interface XYY {
 void get(int i) 
}