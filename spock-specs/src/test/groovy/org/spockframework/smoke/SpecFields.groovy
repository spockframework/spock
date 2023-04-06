/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.smoke

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * @author Peter Niederwieser
 */
class InitializationOfUnsharedFields extends Specification {
  def a
  def b = 1
  int c
  int d = 1

  def setup() {
    assert a == null
    assert b == 1
    assert c == 0
    assert d == 1

    b = 2
  }

  def "feature 1"() {
    expect:
    a == null
    b == 2
    c == 0
    d == 1

    cleanup:
    // should have no effect on subsequent execution
    a = b = c = d = 11111
  }

  def "feature 2"() {
    expect:
    a == null
    b == 2
    c == 0
    d == 1
  }
}

@Stepwise
class InitializationOfSharedFields extends Specification {
  @Shared def a
  @Shared def b = 1
  @Shared int c
  @Shared int d = 1

  def setupSpec() {
    assert a == null
    assert b == 1
    assert c == 0
    assert d == 1

    b = 2
  }

  def setup() {
    assert a == null
    assert b == 2
    assert c == 0 || c == 2 // changed by setup
    assert d == 1 || d == 2 // changed by feature 1

    c = 2
  }

  def "feature 1"() {
    expect:
    a == null
    b == 2
    c == 2
    d == 1

    cleanup:
    d = 2
  }

  def "feature 2"() {
    expect:
    a == null
    b == 2
    c == 2
    d == 2
  }
}

class DefaultValuesOfUnsharedFields extends Specification {
  boolean f1
  char f2
  byte f3
  short f4
  int f5
  long f6
  float f7
  double f8

  Boolean ff1
  Character ff2
  Byte ff3
  Short ff4
  Integer ff5
  Long ff6
  Float ff7
  Double ff8

  def ff9
  Map ff10

  def "primitive types"() {
    expect:
      f1 == false
      f2 == (char)0
      f3 == (byte)0
      f4 == (short)0
      f5 == 0
      f6 == 0l
      f7 == 0f
      f8 == 0d
  }

  def "wrapper types"() {
    expect:
      ff1 == null
      ff2 == null
      ff3 == null
      ff4 == null
      ff5 == null
      ff6 == null
      ff7 == null
      ff8 == null
  }

  def "reference types"() {
    expect:
      ff9 == null
      ff10 == null
  }
}

class DefaultValuesOfSharedFields extends Specification {
  @Shared boolean f1
  @Shared char f2
  @Shared byte f3
  @Shared short f4
  @Shared int f5
  @Shared long f6
  @Shared float f7
  @Shared double f8

  // shared fields that differ only in their capitalization are not allowed, so we add another 'F'
  @Shared Boolean FF1
  @Shared Character FF2
  @Shared Byte FF3
  @Shared Short FF4
  @Shared Integer FF5
  @Shared Long FF6
  @Shared Float FF7
  @Shared Double FF8

  @Shared def FF9
  @Shared Map FF10

  def "primitive types"() {
    expect:
      f1 == false
      f2 == (char)0
      f3 == (byte)0
      f4 == (short)0
      f5 == 0
      f6 == 0l
      f7 == 0f
      f8 == 0d
  }

  def "wrapper types"() {
    expect:
      FF1 == null
      FF2 == null
      FF3 == null
      FF4 == null
      FF5 == null
      FF6 == null
      FF7 == null
      FF8 == null
  }

  def "reference types"() {
    expect:
      FF9 == null
      FF10 == null
  }
}
