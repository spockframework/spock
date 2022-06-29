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

import spock.lang.Issue
import spock.lang.Specification
import static java.util.Collections.reverse
import static org.spockframework.smoke.VoidGroovyStaticMethod.foo

/**
 * @author Peter Niederwieser
 */
@Issue("https://github.com/spockframework/spock/issues/148")
class VoidMethodCallsInThenBlocks extends Specification {
  def "invocation of void Groovy instance method"() {
    when: ""
    then:
    voidGroovyInstanceMethod()
    this.voidGroovyInstanceMethod()
  }

  def "invocation of void Groovy static method"() {
    when: ""
    then:
    voidGroovyStaticMethod()
    VoidMethodCallsInThenBlocks.voidGroovyStaticMethod()
  }

  def "invocation of void Java instance method"() {
    when: ""
    then:
    [].clear()
  }

  def "invocation of void Java static method"() {
    when: ""
    then:
    Collections.shuffle([])
  }

  def "invocation of void default Groovy method"() {
    when: ""
    then:
    print ""
  }

  def "invocation of void default Groovy static method"() {
    when: ""
    then:
    sleep(1)
    Object.sleep(1)
  }

  def "invocation of statically imported void Groovy method"() {
    when: ""
    then:
    foo()
  }

  def "invocation of statically imported void Java method"() {
    when: ""
    then:
    reverse([])
  }

  void voidGroovyInstanceMethod() {}
  static void voidGroovyStaticMethod() {}
}
