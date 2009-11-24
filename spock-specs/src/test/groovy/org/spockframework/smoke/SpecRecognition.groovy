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

import org.spockframework.runtime.SpecUtil
import org.spockframework.runtime.SpockAssertionError

import spock.lang.*
import org.spockframework.EmbeddedSpecification

/**
 * @author Peter Niederwieser
 */
class SpecRecognition extends EmbeddedSpecification {
  def "extending class Specification or a subclass thereof"() {
    when:
    def clazz = compile("""
import spock.lang.Specification

class ASpec extends $baseClass {}
    """)

    then:
    SpecUtil.isSpec(clazz)

    where:
    baseClass << ["Specification", "spock.lang.Specification",
        "org.spockframework.smoke.MySpecification", "org.spockframework.smoke.MyMySpecification"]
  }

  def "missing extends clause"() {
    when:
    def clazz = compile("""
class ASpec {}
    """)

    then:
    !SpecUtil.isSpec(clazz)
  }

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

  // we intentionally don't use EmbeddedSpecCompiler here to avoid confusing matters
  private compile(String source) {
    new GroovyClassLoader().parseClass(source)
  }
}

class MySpecification extends Specification {}

class MyMySpecification extends MySpecification {}

class Intermediary1 extends Specification {}

class Intermediary2 extends spock.lang.Specification {}

abstract class AbstractIntermediary extends Specification {}