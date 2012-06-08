
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

package org.spockframework.util

import spock.lang.*

class ReflectionUtilSpec extends Specification {
  def "getPropertyNameForGetterMethod"() {
    def method = ReflectionUtil.getMethodByName(HasProperties, methodName)

    expect:
    ReflectionUtil.getPropertyNameForGetterMethod(method) == propertyName

    where:
    methodName  | propertyName
    "getLength" | "length"
    "isEmpty"   | "empty"
    "getValid"  | "valid"

    "getLengthStatic" | "lengthStatic"
    "isEmptyStatic"   | "emptyStatic"
    "getValidStatic"  | "validStatic"

    "getURL"    | "URL"
    "getfoo"    | "foo"
    "isfoo"     | "foo"

    "get"       | null
    "is"        | null
    "foo"       | null

    "setFoo"    | null
  }

  static class HasProperties {
    int getLength() {}
    boolean isEmpty() {}
    boolean getValid() {}

    static int getLengthStatic() {}
    static boolean isEmptyStatic() {}
    static boolean getValidStatic() {}

    URL getURL() {}
    boolean getfoo() {}
    boolean isfoo() {}

    String get() {}
    String is() {}
    String foo() {}

    void setFoo(String foo) {}
  }

}

