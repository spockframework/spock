/*
 * Copyright 2012 the original author or authors.
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

import org.spockframework.mock.MockUtil

import spock.lang.Specification

class StubDefaultResponses extends Specification {
  def "returns false for boolean"() {
    def stub = Stub(Stubbable)

    expect:
    !stub.getBoolean()
    !stub.getBooleanWrapper()
  }

  def "returns zero for character"() {
    def stub = Stub(Stubbable)

    expect:
    stub.getChar() == 0
    stub.getCharWrapper() == 0
  }

  def "returns zero for primitive number"() {
    def stub = Stub(Stubbable)

    expect:
    stub.getByte() == 0
    stub.getShort() == 0
    stub.getInt() == 0
    stub.getLong() == 0
    stub.getFloat() == 0
    stub.getDouble() == 0
  }

  def "returns zero for primitive number wrapper"() {
    def stub = Stub(Stubbable)

    expect:
    stub.getByteWrapper() == 0
    stub.getShortWrapper() == 0
    stub.getIntWrapper() == 0
    stub.getLongWrapper() == 0
    stub.getFloatWrapper() == 0
    stub.getDoubleWrapper() == 0
  }

  def "returns zero for big number"() {
    def stub = Stub(Stubbable)

    expect:
    stub.getBigInteger() == BigInteger.ZERO
    stub.getBigDecimal() == BigDecimal.ZERO
  }

  def "returns empty char sequence"() {
    def stub = Stub(Stubbable)

    expect:
    stub.getCharSequence() == ""
    stub.getString() == ""
    stub.getGString() == ""
    stub.getGString() instanceof GString
    stub.getStringBuilder().size() == 0
    stub.getStringBuffer().size() == 0
  }

  def "returns empty array"() {
    def stub = Stub(Stubbable)

    expect:
    stub.getPrimitiveArray() == [] as int[]
    stub.getInterfaceArray() == [] as IPerson[]
    stub.getClassArray() == [] as Person[]
  }

  def "returns empty collection"() {
    def stub = Stub(Stubbable)

    expect:
    stub.getIterable() == []
    stub.getCollection() == []
    stub.getQueue() == []
    stub.getList() == []
    stub.getSet() == [] as Set
    stub.getMap() == [:]
    stub.getSortedSet() == [] as Set
    stub.getSortedMap() == [:]
  }

  def "returns dummy for unknown interface"() {
    def stub = Stub(Stubbable)

    expect:
    with(stub.getUnknownInterface()) {
      new MockUtil().isMock(it)
      name == ""
      age == 0
      children == []
    }
  }

  def "returns newly constructed object for unknown class with default constructor"() {
    def stub = Stub(Stubbable)

    expect:
    with(stub.getUnknownClassWithDefaultCtor()) {
      !new MockUtil().isMock(it)
      name == "default"
      age == 0
      children == null
    }
  }

  def "returns dummy for unknown class without default constructor"() {
    def stub = Stub(Stubbable)

    expect:
    with(stub.getUnknownClassWithoutDefaultCtor()) {
      new MockUtil().isMock(it)
      name == ""
      age == 0
      children == []
    }
  }

  static interface Stubbable {
    byte getByte()
    short getShort()
    int getInt()
    long getLong()
    float getFloat()
    double getDouble()
    boolean getBoolean()
    char getChar()

    Byte getByteWrapper()
    Short getShortWrapper()
    Integer getIntWrapper()
    Long getLongWrapper()
    Float getFloatWrapper()
    Double getDoubleWrapper()
    Boolean getBooleanWrapper()
    Character getCharWrapper()

    BigInteger getBigInteger()
    BigDecimal getBigDecimal()

    CharSequence getCharSequence()
    String getString()
    GString getGString()
    StringBuilder getStringBuilder()
    StringBuffer getStringBuffer()

    int[] getPrimitiveArray()
    IPerson[] getInterfaceArray()
    Person[] getClassArray()

    Iterable getIterable()
    Collection getCollection()
    Queue getQueue()
    List getList()
    Set getSet()
    Map getMap()
    SortedSet getSortedSet()
    SortedMap getSortedMap()

    IPerson getUnknownInterface()
    Person getUnknownClassWithDefaultCtor()
    ImmutablePerson getUnknownClassWithoutDefaultCtor()
  }

  interface IPerson {
    String getName()
    int getAge()
    List<String> getChildren()
  }

  static class Person implements IPerson {
    String name = "default"
    int age
    List<String> children
  }

  static class ImmutablePerson extends Person {
    ImmutablePerson(String name, int age, List<String> children) {
      this.name = name
      this.age = age
      this.children = children
    }
  }
}
