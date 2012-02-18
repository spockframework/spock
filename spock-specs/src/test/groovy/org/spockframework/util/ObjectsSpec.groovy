/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.util

import spock.lang.Specification

class ObjectsSpec extends Specification {
  def equals() {
    expect:
    Objects.equals(null, null)
    !Objects.equals(null, "foo")
    !Objects.equals("foo", null) 
    Objects.equals("foo", "foo")
    !Objects.equals("foo", "bar")
  }
  
  def hashCode() {
    expect:
    Objects.hashCode(null) == 0
    Objects.hashCode("foo") == "foo".hashCode()
  }
  
  def toString() {
    expect:
    Objects.toString(null) == "null"
    Objects.toString("foo") == "foo"
  }
  
  def getClass() {
    expect:
    Objects.getClass(null) == null
    Objects.getClass("foo") == String
  }
  
  def voidAwareGetClass() {
    expect:
    Objects.voidAwareGetClass(null) == void
    Objects.voidAwareGetClass("foo") == String
  }
  
  def eitherNull() {
    expect:
    Objects.eitherNull(null, null)
    Objects.eitherNull("foo", null)
    Objects.eitherNull(null, "bar")
    !Objects.eitherNull("foo", "bar")
  }
  
  def compare() {
    expect:
    Objects.compare(null, null) == 0
    Objects.compare(null, 3) < 0
    Objects.compare(3, null) > 0
    Objects.compare(3, 3) == 0
    Objects.compare(3, 4) < 0
    Objects.compare(4, 3) > 0
  }
}
