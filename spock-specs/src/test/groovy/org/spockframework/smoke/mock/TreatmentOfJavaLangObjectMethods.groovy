/*
 * Copyright 2009 the original author or authors.
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

import spock.lang.*

@Ignore
class TreatmentOfJavaLangObjectMethods extends Specification {
  Object mock = Mock()

  def "equals()"() {
    expect:
    !mock.equals(mock)
  }

  @FailsWith(value = NullPointerException, reason = "equals() is dispatched to GDK.equals(List, List), which calls methods like iterator(); not sure how to solve this problem")
  def "GDK equals()"() {
    List list = Mock()

    expect:
    !list.equals(list)
  }

  def "hashCode()"() {
    expect:
    mock.hashCode() == 0
  }

  def "toString()"() {
    expect:
    mock.toString() == null
  }
}
