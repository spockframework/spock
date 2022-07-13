/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.util

import spock.lang.Specification

class ObjectUtilSpec extends Specification {
  def "null aware equals"() {
    expect:
    ObjectUtil.equals(null, null)
    !ObjectUtil.equals(null, "foo")
    !ObjectUtil.equals("foo", null)
    ObjectUtil.equals("foo", "foo")
    !ObjectUtil.equals("foo", "bar")
  }

  def "null aware hashCode"() {
    expect:
    ObjectUtil.hashCode(null) == 0
    ObjectUtil.hashCode("foo") == "foo".hashCode()
  }

  def "null aware toString"() {
    expect:
    ObjectUtil.toString(null) == "null"
    ObjectUtil.toString("foo") == "foo"
  }

  def "null aware getClass"() {
    expect:
    ObjectUtil.getClass(null) == null
    ObjectUtil.getClass("foo") == String
  }

  def "void aware getClass"() {
    expect:
    ObjectUtil.voidAwareGetClass(null) == void
    ObjectUtil.voidAwareGetClass("foo") == String
  }

  def eitherNull() {
    expect:
    ObjectUtil.eitherNull(null, null)
    ObjectUtil.eitherNull("foo", null)
    ObjectUtil.eitherNull(null, "bar")
    !ObjectUtil.eitherNull("foo", "bar")
  }

  def compare() {
    expect:
    ObjectUtil.compare(null, null) == 0
    ObjectUtil.compare(null, 3) < 0
    ObjectUtil.compare(3, null) > 0
    ObjectUtil.compare(3, 3) == 0
    ObjectUtil.compare(3, 4) < 0
    ObjectUtil.compare(4, 3) > 0
  }
}
