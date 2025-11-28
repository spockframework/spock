/*
 * Copyright 2025 the original author or authors.
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

package org.spockframework.util

import spock.lang.Specification

class ClassUtilSpec extends Specification {
  def "get string representation of non-null class"() {
    expect:
    ClassUtil.nullSafeToString(clazz) == clazz.getName()

    where:
    clazz << [Set, Tuple, ClassUtil]
  }

  def "get string representation of null class"() {
    when:
    String result = ClassUtil.nullSafeToString(null)

    then:
    result == "null"
  }

  def "get string representation of multiple potentially null classes"(Class<?>... clazzes) {
    given:
    def clazzStrings = []
    for (clazz in clazzes) {
      clazzStrings.add(ClassUtil.nullSafeToString(clazz))
    }

    when:
    String result = ClassUtil.allNullSafeToString(clazzes)

    then: "result is names from single class overload separated by a comma and space each"
    notThrown(NullPointerException)
    result == String.join(", ", clazzStrings)

    where:
    clazzes << [[List, Set, Queue, Map],
                [File, null, URL],
                [null, null]]
  }

  def "get string representation of one class not known to be just one"() {
    given:
    Class<?>[] singleton = [Specification.class]
    Class<?>[] singletonOfNull = [null]

    expect:
    ClassUtil.allNullSafeToString(singleton) == ClassUtil.nullSafeToString(Specification.class)
    ClassUtil.allNullSafeToString(singletonOfNull) == ClassUtil.nullSafeToString(null)
  }

  def "get string representation of no classes"() {
    expect:
    ClassUtil.allNullSafeToString() == ""
  }

  def "get string representation of null array of classes"() {
    given:
    Class<?>[] nullArray = null

    when:
    String result = ClassUtil.allNullSafeToString(nullArray)

    then:
    result == ""
  }
}
