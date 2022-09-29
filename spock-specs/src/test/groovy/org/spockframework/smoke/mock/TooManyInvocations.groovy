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

import org.spockframework.EmbeddedSpecification
import org.spockframework.mock.TooManyInvocationsError

class TooManyInvocations extends EmbeddedSpecification {
  def "shows matched invocations, ordered by last occurrence"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)

when:
list.add(2)
list.add(1)
list.add(4)
list.add(2)

then:
3 * list.add(_)
    """)

    then:
    TooManyInvocationsError e = thrown()
    e.message.trim() == """
Too many invocations for:

3 * list.add(_)   (4 invocations)

Matching invocations (ordered by last occurrence):

2 * list.add(2)   <-- this triggered the error
1 * list.add(4)
1 * list.add(1)
    """.trim()
  }

  def "casts are printed for mock invocations"() {
    given:
    runner.addClassImport(SomeStatic)

    when:
    runner.runFeatureBody """
GroovyMock(SomeStatic, global: true)

when:
SomeStatic.compile(null as String)

then:
0 * SomeStatic.compile(_)
"""

    then:
    TooManyInvocationsError e = thrown()
    e.message.contains('<SomeStatic>.compile(null as String)')
  }
}

class SomeStatic {
  static String compile(String str){
    str
  }
}
