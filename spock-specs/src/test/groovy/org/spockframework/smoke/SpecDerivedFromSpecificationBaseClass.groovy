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

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.SpockAssertionError
import org.spockframework.util.SyntaxException
import spock.util.EmbeddedSpecCompiler
import spock.lang.*

/**
 * @author Peter Niederwieser
 */
// TODO: find suitable name, maybe split into two specs
class SpecDerivedFromSpecificationBaseClass extends EmbeddedSpecification {
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
    thrown(SpockAssertionError)

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
    def compiler = new EmbeddedSpecCompiler()
    def classes = compiler.compile("""
import spock.lang.Specification

class MyCustomBaseClass extends Specification {}

class Foo extends MyCustomBaseClass {
  def foo() {
    expect: false
  }
}
    """)

    def fooClass = classes.find { it.name == "Foo" }

    when:
    runner.runClass(fooClass)

    then:
    thrown(SpockAssertionError)
  }

  def "can refer to Specification members by simple name"() {
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

  @FailsWith(value = SyntaxException, reason = "TODO")
  def "can refer to Specification members by 'this' and 'super'"() {
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
    noExceptionThrown()

    where:
    target << ["this", "super"]
  }

  @Issue("http://issues.spockframework.org/detail?id=43")
  def "can use Specification members in field initializers"() {
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