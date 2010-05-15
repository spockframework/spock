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

package org.spockframework.smoke.mock

import spock.lang.*
import org.spockframework.EmbeddedSpecification
import org.codehaus.groovy.syntax.SyntaxException

class InvalidInteractions extends EmbeddedSpecification {
  @Unroll
  def "stubbing/mocking static methods and properties is not supported"() {
    when:
    compiler.compile("""
class Foo {
  static void getFoo() {}
}

class Bar extends spock.lang.Specification {
  def bar() {
    setup:
    $interaction
  }
}
    """)

    then:
    SyntaxException e = thrown()
    e.message.toLowerCase().contains("static")

    where:
    interaction << ["1 * System.exit()", "Foo.getFoo() >> true", "Foo.foo >> true", "Foo.getBar() >> true", "Foo.bar >> true"]
  }
}

