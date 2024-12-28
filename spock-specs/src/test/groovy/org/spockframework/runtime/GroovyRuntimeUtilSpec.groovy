/*
* Copyright 2010 the original author or authors.
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

package org.spockframework.runtime

import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import spock.lang.Specification

import static org.spockframework.runtime.GroovyRuntimeUtil.propertyToBooleanGetterMethodName
import static org.spockframework.runtime.GroovyRuntimeUtil.propertyToGetterMethodName
import static org.spockframework.runtime.GroovyRuntimeUtil.propertyToSetterMethodName

class GroovyRuntimeUtilSpec extends Specification {
  def "getterMethodToPropertyName"() {
    expect:
    GroovyRuntimeUtil.getterMethodToPropertyName(methodName, [], returnType) == propertyName

    where:
    methodName        | returnType | propertyName
    "getLength"       | Integer    | "length"
    "isEmpty"         | boolean    | "empty"
    "getValid"        | boolean    | "valid"

    "getLengthStatic" | Integer    | "lengthStatic"
    "isEmptyStatic"   | boolean    | "emptyStatic"
    "getValidStatic"  | boolean    | "validStatic"

    "getURL"          | URL        | "URL"
    "getfoo"          | String     | "foo"
    "isfoo"           | boolean    | "foo"

    "get"             | Integer    | null
    "is"              | boolean    | null
    "foo"             | String     | null
    "isfoo"           | String     | null

    "setFoo"          | void       | null
  }

  def "propertyToGetterMethodName"() {
    expect:
    propertyToGetterMethodName("prop") == "getProp"
  }

  def "propertyToSetterMethodName"() {
    expect:
    propertyToSetterMethodName("prop") == "setProp"
  }

  def "propertyToBooleanGetterMethodName"() {
    expect:
    propertyToBooleanGetterMethodName("prop") == "isProp"
  }

  def "coerce"() {
    expect:
    GroovyRuntimeUtil.coerce("x", Character) == "x" as Character
    GroovyRuntimeUtil.coerce([3, 1, 2], Set) == [1, 2, 3] as Set

    when:
    GroovyRuntimeUtil.coerce([3, 1, 2], Number)

    then:
    thrown(GroovyCastException)
  }

  def "coercing to primitive type has same effect as coercing to wrapper type (and doesn't blow up)"() {
    expect:
    GroovyRuntimeUtil.coerce("x", char) == "x"
    GroovyRuntimeUtil.coerce("x", char) instanceof Character

    GroovyRuntimeUtil.coerce(123, byte) == 123
    GroovyRuntimeUtil.coerce(123, byte) instanceof Byte
  }

  def "coerce to first successful candidate type"() {
    expect:
    GroovyRuntimeUtil.coerce("123", Number, String) == "123"
    GroovyRuntimeUtil.coerce(123, Number, String) == 123

    when:
    GroovyRuntimeUtil.coerce(123, List, Set)

    then:
    thrown(GroovyCastException)
  }

  def "instantiate closure"() {
    def owner = [x : 1]
    def thisObject = new Object() {
      def y = 2
    }
    def cl = { z -> x + this.y + z }

    expect:
    GroovyRuntimeUtil.instantiateClosure(cl.getClass(), owner, thisObject).call(3) == 6
  }

  def "asArgumentList"() {
    when:
    def l = GroovyRuntimeUtil.asArgumentList(null)
    then:
    l.isEmpty()
    when:
    l = GroovyRuntimeUtil.asArgumentList("A")
    then:
    l.size() == 1
    l == ["A"]
  }
}

