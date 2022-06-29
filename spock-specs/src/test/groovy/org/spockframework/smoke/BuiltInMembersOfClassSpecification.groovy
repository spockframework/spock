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

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.InvalidSpecException
import org.spockframework.util.GroovyReleaseInfo
import org.spockframework.util.VersionNumber
import spock.lang.*

/**
 * @author Peter Niederwieser
 */
class BuiltInMembersOfClassSpecification extends EmbeddedSpecification {
  def "can be referred to by simple name"() {
    when:
    runner.run """
class Foo extends spock.lang.Specification {
  def foo() {
    when:
    throw new Exception()

    then:
    thrown(Exception)
  }
}
    """

    then:
    noExceptionThrown()
  }

  def "can be referred to by 'this'"() {
    when:
    runner.run """
class Foo extends spock.lang.Specification {
  def foo() {
    def list = this.Mock(List)

    expect:
    list != null
  }
}
    """

    then:
    noExceptionThrown()
  }

  @Requires({GroovyReleaseInfo.version <= VersionNumber.parse("2.3.99")})
  def "cannot be referred to by 'super'"() {
    when:
    runner.run """
class Foo extends spock.lang.Specification {
  def foo() {
    def list = super.Mock(List)

    expect:
    list != null
  }
}
    """

    then:
    thrown(IllegalAccessError)
  }

  @Issue("https://github.com/spockframework/spock/issues/166")
  def "can be used in field initializers"() {
    when:
    runner.run """
class Foo extends spock.lang.Specification {
  def list = Mock(List)

  def foo() { expect: true }
}
    """

    then:
    notThrown(InvalidSpecException)
  }
}
