/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke

import org.junit.runner.RunWith
import org.spockframework.runtime.SpeckAssertionError
import org.spockframework.util.SyntaxException
import spock.lang.*
import static spock.lang.Predef.noExceptionThrown
import static spock.lang.Predef.thrown

/**
 * @author Peter Niederwieser
 */
@Speck
@RunWith(Sputnik)
class SpeckThatExtendsSpockLangSpecification {
  def runner = new EmbeddedSpeckRunner()

  def "is properly recognized"() {
    when:
    runner.run """
class Foo extends spock.lang.Specification {
  def foo() {
    expect: false
  }
}
    """

    then:
    thrown(SpeckAssertionError)
  }

  def "can refer to Predef members by simple name"() {
    when:
    runner.run """
$collidingImport

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

    where:
    collidingImport << ["", "import static spock.lang.Predef.thrown", "import static spock.lang.Predef.*"]
  }

  def "can refer to Predef members by 'Predef.'"() {
    when:
    runner.run """
$collidingImport
import spock.lang.Predef

class Foo extends spock.lang.Specification {
  def foo() {
    def list = []

    when:
    list.add("elem")

    then:
    list.size() == Predef.old(list.size()) + 1
  }
}
    """

    then:
    noExceptionThrown()

    where:
    collidingImport << ["", "import static spock.lang.Predef.old", "import static spock.lang.Predef.*"]
  }


  def "can refer to Predef members by 'spock.lang.Predef.'"() {
    when:
    runner.run """
$collidingImport

class Foo extends spock.lang.Specification {
  def foo() {
    def list = spock.lang.Predef.Mock(List)

    expect:
    list != null
  }
}
    """

    then:
    noExceptionThrown()

    where:
    collidingImport << ["", "import static spock.lang.Predef.Mock", "import static spock.lang.Predef.*"]
  }

  def "can refer to Predef members by 'this.' (due to a Groovy bug)"() {
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

  def "cannot refer to Predef members by 'Foo.', or 'Specification.'"() {
    when:
    runner.run """
class Foo extends spock.lang.Specification {
  def foo() {
    def list = ${target}.Mock(List)

    expect:
    list != null
  }
}
    """

    then:
    thrown(SyntaxException)

    where:
    target << ["Foo", "spock.lang.Specification"]
  }
}