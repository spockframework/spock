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

package org.spockframework.runtime

import spock.lang.*
import org.spockframework.runtime.GroovyRuntimeUtil

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

    "setFoo"          | void       | null
  }
}

