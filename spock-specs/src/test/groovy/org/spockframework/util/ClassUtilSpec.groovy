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
  def "get string representation of class"() {
    expect:
    ClassUtil.nullSafeToString(clazz as Class<?>) == expectedString

    where:
    clazz     | expectedString
    Set       | "java.util.Set"
    Tuple     | "groovy.lang.Tuple"
    ClassUtil | "org.spockframework.util.ClassUtil"
    null      | "null"
  }

  def "get string representation of none, one, or multiple potentially null classes"(Class<?>[] clazzes, String expectedString) {
    expect: "result is names from single class overload separated by a comma and space each"
    ClassUtil.nullSafeToString(clazzes as Class<?>[]) == expectedString

    where:
    clazzes                 | expectedString
    [List, Set, Queue, Map] | "java.util.List, java.util.Set, java.util.Queue, java.util.Map"
    [File, null, URL]       | "java.io.File, null, java.net.URL"
    [null, null]            | "null, null"
    [File]                  | "java.io.File"
    []                      | ""
    null                    | ""
  }
}
