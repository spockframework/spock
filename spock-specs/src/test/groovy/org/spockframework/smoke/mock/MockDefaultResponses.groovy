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

package org.spockframework.smoke.mock

import spock.lang.Specification

class MockDefaultResponses extends Specification {
  IMockable imock = Mock()
  Mockable cmock = Mock()

  def "return values for unmatched invocations on interface-based mocks"() {
    expect:
    imock.getBoolean() == false
    imock.getByte() == 0
    imock.getShort() == 0
    imock.getInt() == 0
    imock.getLong() == 0
    imock.getFloat() == 0f
    imock.getDouble() == 0d
    imock.getObject() == null
    imock.getVoid() == null
    imock.getDynamic() == null
  }

  def "types of return values for unmatched invocations on interface-based mocks"() {
    expect:
    imock.getBoolean().getClass() == Boolean.class
    imock.getByte().getClass() == Byte.class
    imock.getShort().getClass() == Short.class
    imock.getInt().getClass() == Integer.class
    imock.getLong().getClass() == Long.class
    imock.getFloat().getClass() == Float.class
    imock.getDouble().getClass() == Double.class
  }

    def "return values for unmatched invocations on class-based mocks"() {
    expect:
    cmock.getBoolean() == false
    cmock.getByte() == 0
    cmock.getShort() == 0
    cmock.getInt() == 0
    cmock.getLong() == 0
    cmock.getFloat() == 0f
    cmock.getDouble() == 0d
    cmock.getObject() == null
    cmock.getVoid() == null
    cmock.getDynamic() == null
  }

  def "types of return values for unmatched invocations on class-based mocks"() {
    expect:
    cmock.getBoolean().getClass() == Boolean.class
    cmock.getByte().getClass() == Byte.class
    cmock.getShort().getClass() == Short.class
    cmock.getInt().getClass() == Integer.class
    cmock.getLong().getClass() == Long.class
    cmock.getFloat().getClass() == Float.class
    cmock.getDouble().getClass() == Double.class
  }

  interface IMockable {
    boolean getBoolean()
    byte getByte()
    short getShort()
    int getInt()
    long getLong()
    float getFloat()
    double getDouble()
    Object getObject()
    void getVoid()
    def getDynamic()
  }

  static class Mockable {
    boolean getBoolean() { true }
    byte getByte() { 42 as byte }
    short getShort() { 42 as short }
    int getInt() { 42 as int }
    long getLong() { 42 as long }
    float getFloat() { 42 as float }
    double getDouble() { 42 as double }
    Object getObject() { new Object() }
    void getVoid() {}
    def getDynamic() { 42 }
  }
}

