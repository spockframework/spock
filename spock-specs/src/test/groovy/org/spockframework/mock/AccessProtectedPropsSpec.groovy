/*
 * Copyright 2023 the original author or authors.
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

package org.spockframework.mock

import spock.lang.Issue
import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
class AccessProtectedPropsSpec extends Specification {

  /**
   * The problem with https://github.com/spockframework/spock/issues/1501 is that the ByteBuddyMockFactory overrides
   * the {@code getProperty(String)} method from GroovyObject without having the {@code groovy.transform.Internal}
   * annotation. Therefore {@code org.codehaus.groovy.runtime.callsite.AbstractCallSite.createGroovyObjectGetPropertySite(java.lang.Object)}
   * takes another code path.
   */
  @Issue("https://github.com/spockframework/spock/issues/1501")
  @Issue("https://github.com/spockframework/spock/issues/1608")
  def "Access protected const should be accessible in Groovy 3&4 Issue #1501"() {
    when:
    AccessProtectedSubClass mySpy = Spy()
    then:
    mySpy.accessStaticFlag()
  }

  @Issue("https://github.com/spockframework/spock/issues/1501")
  @Issue("https://github.com/spockframework/spock/issues/1608")
  def "Access protected should be accessible in Groovy 3&4 Issue #1501"() {
    when:
    AccessProtectedSubClass mySpy = Spy()
    then:
    mySpy.accessNonStaticFlag()
  }

  /**
   * This feature must run after the {@code Access protected const should be accessible in Groovy 3&4 Issue #1501},
   * otherwise the MetaClass gets cached in the GroovyRuntime, therefore we need the {@code @Stepwise}.
   */
  def "Access protected fields via access methods without spy"() {
    when:
    AccessProtectedSubClass myNonSpy = new AccessProtectedSubClass()
    then:
    myNonSpy.accessNonStaticFlag()
    myNonSpy.accessStaticFlag()
  }

  @Issue("https://github.com/spockframework/spock/issues/1501")
  def "Access protected fields should be accessible in Groovy 3&4 with Java class Issue #1501"() {
    when:
    AccessProtectedJavaSubClass mySpy = Spy()
    then:
    mySpy.accessNonStaticFlag()
  }

  @Issue("https://github.com/spockframework/spock/issues/1452")
  def "Access protected fields Issue #1452"() {
    when:
    AccessProtectedSubClass mySpy = Spy(AccessProtectedSubClass)
    then:
    mySpy.nonStaticFlag
    mySpy.staticFlag
  }

  def "Access protected fields without spy"() {
    when:
    AccessProtectedSubClass mySpy = new AccessProtectedSubClass()
    then:
    mySpy.nonStaticFlag
    mySpy.staticFlag
  }
}

class AccessProtectedBaseClass {
  protected static boolean staticFlag = true
  protected boolean nonStaticFlag = true
}

class AccessProtectedSubClass extends AccessProtectedBaseClass {
  boolean accessNonStaticFlag() {
    return nonStaticFlag
  }

  @SuppressWarnings('GrMethodMayBeStatic')
  boolean accessStaticFlag() {
    return staticFlag
  }
}
