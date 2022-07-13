/*
 * Copyright 2020 the original author or authors.
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

package org.spockframework.smoke.parameterization

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.InvalidSpecException
import org.spockframework.runtime.SpockExecutionException
import spock.lang.Rollup
import spock.lang.Specification
import spock.lang.Unroll

class UprolledFeatureMethods extends EmbeddedSpecification {
  def setup() {
    runner.addClassImport(Actor)
  }

  def "iterations of an uprolled feature do not count as separate tests"() {
    when:
    def result = runner.runSpecBody("""
@Rollup
def foo() {
  expect: true

  where:
  x << [1, 2, 3]
}
    """)

    then:
    result.testsStartedCount == 1
    result.testsAbortedCount == 0
    result.testsSucceededCount == 1
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
  }

  def "a feature with an empty data provider causes the same error regardless if it's unrolled or not"() {
    when:
    runner.runSpecBody """
$annotation
def foo() {
  expect: true

  where:
  x << []
}
    """

    then:
    SpockExecutionException e = thrown()

    where:
    annotation << ["", "@Rollup"]
  }

  def "can roll up a whole class at once"() {
    when:
    def result = runner.runWithImports("""
@Rollup
class Foo extends Specification {
  def "#actor.details.name"() {
    expect: true

    where:
    actor = new Actor()
  }

  def "not data-driven"() {
    expect: true
  }

  def "#actor.details.age"() {
    expect: true

    where:
    actor = new Actor()
  }
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["#actor.details.name",
                                                                        "not data-driven",
                                                                        "#actor.details.age"]
  }

  def "method-level uproll annotation wins over class-level unroll annotation"() {
    when:
    def result = runner.runWithImports("""
@Unroll("#actor.details.name")
class Foo extends Specification {
  @Rollup
  def method() {
    expect: true

    where:
    actor = new Actor()
  }
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["method"]
  }

  def "unroll and uproll cannot be combined on the same spec"() {
    when:
    runner.runWithImports("""
@Unroll("#actor.details.name")
@Rollup
class Foo extends Specification {
  def method() {
    expect: true

    where:
    actor = new Actor()
  }
}
    """)

    then:
    thrown(InvalidSpecException)
  }

  def "unroll and uproll cannot be combined on the same feature"() {
    when:
    runner.runSpecBody("""
@Unroll("#actor.details.name")
@Rollup
def method() {
  expect: true

  where:
  actor = new Actor()
}
    """)

    then:
    thrown(InvalidSpecException)
  }

  def "method-level uproll annotation wins over configuration script"() {
    given:
    runner.configurationScript {
      unroll {
        unrollByDefault true
      }
    }

    when:
    def result = runner.runSpecBody("""
@Rollup
def method() {
  expect: true

  where:
  actor = new Actor()
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["method"]
  }

  def "class-level uproll annotation wins over configuration script"() {
    given:
    runner.configurationScript {
      unroll {
        unrollByDefault true
      }
    }

    when:
    def result = runner.runWithImports("""
@Rollup
class Foo extends Specification {
  def method() {
    expect: true

    where:
    actor = new Actor()
  }
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["method"]
  }

  def "rollup annotations are not inheritable"() {
    given:
    runner.configurationScript {
      unroll {
        defaultPattern '#featureName: #actor.details.name'
      }
    }
    runner.addClassImport(Base)

    when:
    def result = runner.runWithImports("""
class Sub extends Base {
  def methodSub() {
    expect: true

    where:
    actor = new Actor()
  }
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == [
      "methodRoot",
      "methodRoot: fred",
      "methodBase",
      "methodSub",
      "methodSub: fred"
    ]
  }

  static class Actor {
    Map details = [name: "fred", age: 30]
  }

  static class Root extends Specification {
    def methodRoot() {
      expect: true

      where:
      actor = new Actor()
    }
  }

  @Rollup
  static class Base extends Root {
    def methodBase() {
      expect: true

      where:
      actor = new Actor()
    }
  }
}
