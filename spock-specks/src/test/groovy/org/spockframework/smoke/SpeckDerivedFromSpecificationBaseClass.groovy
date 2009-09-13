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

import org.spockframework.runtime.SpeckAssertionError
import org.spockframework.util.SyntaxException
import spock.lang.Specification
import spock.lang.Unroll
import spock.lang.Issue
import spock.util.EmbeddedSpecification
import spock.util.EmbeddedSpeckCompiler

/**
 * @author Peter Niederwieser
 */
class SpeckDerivedFromSpecificationBaseClass extends EmbeddedSpecification {
  @Unroll
  def "is properly recognized"() {
    when:
    runner.run """
$importStat

class Foo extends $baseClass {
  def foo() {
    expect: false
  }
}
    """

    then:
    thrown(SpeckAssertionError)

    where:
    [importStat                                     , baseClass                                      ] << [
    ["import spock.lang.Specification"              , "Specification"                                ],
    [""                                             , "spock.lang.Specification"                     ],
    ["import org.spockframework.smoke.Intermediary1", "Intermediary1"                                ],
    [""                                             , "org.spockframework.smoke.Intermediary2"       ],
    [""                                             , "org.spockframework.smoke.AbstractIntermediary"]
    ]
  }

  def "is properly recognized when inheriting from Specification base class through intermediary in same compilation unit"() {
    def compiler = new EmbeddedSpeckCompiler()
    compiler.compile """
import spock.lang.Specification

class MyCustomBaseClass extends Specification {}

class Foo extends MyCustomBaseClass {
  def foo() {
    expect: false
  }
}
    """

    def fooClass = compiler.loader.loadedClasses.find { it.name == "Foo" }

    when:
    runner.runClass(fooClass)

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

  @Unroll("cannot refer to Predef members by '#target'")
  def "cannot refer to Predef members by other means"() {
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
    target << ["this", "super", "Foo", "spock.lang.Specification"]
  }

  @Issue("http://issues.spockframework.org/detail?id=43")
  def "can use Predef members in field initializers"() {
    when:
    runner.run """
class Foo extends spock.lang.Specification {
  def list = Mock(List)

  def foo() { expect: true }
}
    """

    then:
    notThrown(SyntaxException)
  }
}

class Intermediary1 extends Specification {}

class Intermediary2 extends spock.lang.Specification {}

abstract class AbstractIntermediary extends Specification {}