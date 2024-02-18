/*
 * Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.smoke

import org.spockframework.runtime.model.parallel.Resources
import spock.lang.Issue
import spock.lang.ResourceLock
import spock.lang.Specification

import static java.util.Collections.reverse
import static org.spockframework.smoke.VoidGroovyStaticMethod.foo

/**
 * @author Peter Niederwieser
 */
@Issue("https://github.com/spockframework/spock/issues/148")
class VoidMethodCallsInExpectBlocks extends Specification {
  def "invocation of void Groovy instance method"() {
    expect:
    voidGroovyInstanceMethod()
    this.voidGroovyInstanceMethod()
  }

  def "invocation of void Groovy static method"() {
    expect:
    voidGroovyStaticMethod()
    VoidMethodCallsInExpectBlocks.voidGroovyStaticMethod()
  }

  def "invocation of void Java instance method"() {
    expect:
    [].clear()
  }

  def "invocation of void Java static method"() {
    expect:
    Collections.shuffle([])
  }

  @ResourceLock(Resources.SYSTEM_OUT)
  def "invocation of void default Groovy method"() {
    expect:
    print ""
  }

  def "invocation of void default Groovy static method"() {
    expect:
    sleep(1)
    Object.sleep(1)
  }

  def "invocation of statically imported void Groovy method"() {
    expect:
    foo()
  }

  def "invocation of statically imported void Java method"() {
    expect:
    reverse([])
  }

  void voidGroovyInstanceMethod() {}

  static void voidGroovyStaticMethod() {}
}
