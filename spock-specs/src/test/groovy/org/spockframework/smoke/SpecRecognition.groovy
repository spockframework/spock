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

import org.spockframework.runtime.SpockAssertionError

import spock.lang.*
import org.spockframework.EmbeddedSpecification

/**
 * @author Peter Niederwieser
 */
class SpecRecognition extends EmbeddedSpecification {
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
    def classes = compiler.compile("""
import spock.lang.Specification

class MyCustomBaseClass extends Specification {}

class Foo extends MyCustomBaseClass {
  def foo() {
    expect: false
  }
}
    """)

    def clazz = classes.find { it.name == "Foo" }

    when:
    runner.runClass(clazz)

    then:
    thrown(SpockAssertionError)
  }
}

class MySpecification extends Specification {}

class MyMySpecification extends MySpecification {}

class Intermediary1 extends Specification {}

class Intermediary2 extends spock.lang.Specification {}

abstract class AbstractIntermediary extends Specification {}
