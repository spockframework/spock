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

package org.spockframework.smoke.parameterization

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.extension.*
import org.spockframework.runtime.model.*
import spock.lang.*

import java.lang.annotation.*

/**
 * @author Peter Niederwieser
 */
class UnrolledFeatureMethods extends EmbeddedSpecification {

  def setup() {
    runner.addClassImport(Actor)
    runner.addClassImport(Actor2)
  }

  def "iterations of an unrolled feature count as separate tests"() {
    when:
    def result = runner.runFeatureBody("""
expect: true

where:
x << [1, 2, 3]
    """)

    then:
    result.testsSucceededCount == 4
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
  }

  def "iterations of an unrolled feature foo are named 'foo [x: 1, #0]', 'foo [x: 2, #1]', etc."() {
    when:
    def result = runner.runSpecBody """
def foo() {
  expect: true

  where:
  x << [1, 2, 3]
}
    """

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["foo"] + (0..2).collect {"foo [x: ${it + 1}, #$it]" }
  }

  def "if creation of a data provider fails, feature isn't unrolled"() {
    runner.throwFailure = false

    when:
    def result = runner.runFeatureBody("""
expect: true

where:
x << [1]
y << { throw new Exception() }()
    """)

    then:

    result.testsSucceededCount == 0
    result.testsFailedCount == 1
    result.testsSkippedCount == 0
    result.containersStartedCount == 1 + 1 + 1 // engine + spec + unrolled feature
    result.containersFailedCount == 1
    result.containerEvents().failed().list().testDescriptor.displayName == ["a feature"]
  }

  def "naming pattern may refer to data variables"() {
    when:
    def result = runner.runSpecBody("""
@Unroll("one #y two #x three")
def foo() {
  expect: true

  where:
  x << [1, 2, 3]
  y << ["a", "b", "c"]
}
    """)

    then:

    result.testEvents().started().list().testDescriptor.displayName == ["foo",
                                                                        "one a two 1 three",
                                                                        "one b two 2 three",
                                                                        "one c two 3 three"]
  }

  def "naming pattern may refer to feature name and iteration index"() {
    when:
    def result = runner.runSpecBody("""
@Unroll("one #featureName two #iterationIndex three")
def foo() {
  expect: true

  where:
  x << [1, 2, 3]
  y << ["a", "b", "c"]
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["foo",
                                                                        "one foo two 0 three",
                                                                        "one foo two 1 three",
                                                                        "one foo two 2 three"]
  }

  def "naming pattern may refer to dataVariables"() {
    when:
    def result = runner.runSpecBody("""
@Unroll("one #dataVariables two")
def foo() {
  expect: true

  where:
  x << [1, 2, 3]
  y << ["a", "b", "c"]
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["foo",
                                                                        "one x: 1, y: a two",
                                                                        "one x: 2, y: b two",
                                                                        "one x: 3, y: c two"]
  }

  def "naming pattern may refer to dataVariablesWithIndex"() {
    when:
    def result = runner.runSpecBody("""
@Unroll("one #dataVariablesWithIndex two")
def foo() {
  expect: true

  where:
  x << [1, 2, 3]
  y << ["a", "b", "c"]
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["foo",
                                                                        "one x: 1, y: a, #0 two",
                                                                        "one x: 2, y: b, #1 two",
                                                                        "one x: 3, y: c, #2 two"]
  }

  def "old iteration naming is restorable using configuration script"() {
    given:
    runner.configurationScript {
      unroll {
        defaultPattern '#featureName[#iterationIndex]'
      }
    }

    when:
    def result = runner.runSpecBody("""
def foo() {
  expect: true

  where:
  x << [1, 2, 3]
  y << ["a", "b", "c"]
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["foo",
                                                                        "foo[0]",
                                                                        "foo[1]",
                                                                        "foo[2]"]
  }

  def "default iteration naming is configurable using configuration script"() {
    given:
    runner.configurationScript {
      unroll {
        defaultPattern '#featureName'
      }
    }

    when:
    def result = runner.runSpecBody("""
def foo() {
  expect: true

  where:
  x << [1, 2, 3]
  y << ["a", "b", "c"]
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["foo",
                                                                        "foo",
                                                                        "foo",
                                                                        "foo"]
  }

  def "includeFeatureNameForIterations is configurable using configuration script"() {
    given:
    runner.configurationScript {
      unroll {
        includeFeatureNameForIterations false
      }
    }

    when:
    def result = runner.runSpecBody("""
def foo() {
  expect: true

  where:
  x << [1, 2, 3]
  y << ["a", "b", "c"]
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["foo",
                                                                        "x: 1, y: a, #0",
                                                                        "x: 2, y: b, #1",
                                                                        "x: 3, y: c, #2"
                                                                        ]
  }

  def "dataVariablesWithIndex can be used in @Unroll"() {
    when:
    def result = runner.runSpecBody("""
@Unroll("#dataVariablesWithIndex")
def foo() {
  expect: true

  where:
  x << [1, 2, 3]
  y << ["a", "b", "c"]
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["foo",
                                                                        "x: 1, y: a, #0",
                                                                        "x: 2, y: b, #1",
                                                                        "x: 3, y: c, #2"
                                                                        ]
  }

  @Issue("https://github.com/spockframework/spock/issues/187")
  def "variables in naming pattern whose value is null are replaced correctly"() {
    when:
    def result = runner.runSpecBody("""
@Unroll("one #x two #y three")
def foo() {
  expect: true

  where:
  x << [1]
  y << [null]
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["foo",
                                                                        "one 1 two null three"]
  }

  @Issue("https://github.com/spockframework/spock/issues/353")
  def "naming pattern supports property expressions"() {
    when:
    def result = runner.runSpecBody("""
@Unroll("one #actor.details.name two")
def foo() {
  expect: true

  where:
  actor = new Actor()
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["foo",
                                                                        "one fred two"]
  }

  @Issue("https://github.com/spockframework/spock/issues/353")
  def "naming pattern supports zero-arg method calls"() {
    when:
    def result = runner.runSpecBody("""
@Unroll("one #actor.details.name.size() two")
def foo() {
  expect: true

  where:
  actor = new Actor()
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["foo",
                                                                        "one 4 two"]
  }

  def "expressions in naming pattern that can't be evaluated are prefixed with '#Error:' if expressions asserting is disabled"() {
    given:
    runner.configurationScript {
      unroll {
        validateExpressions false
      }
    }

    when:
    def result = runner.runSpecBody("""
@Unroll("#obj #obj.ok() #obj.bang() #obj.missing() #missing")
def foo() {
  expect: true

  where:
  obj = { def obj = new Expando(); obj.ok = { "ok" }; obj.toString = { throw new RuntimeException() }; obj.bang = { throw new NullPointerException() }; obj }()
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["foo",
                                                                        "#Error:obj ok #Error:obj.bang() #Error:obj.missing() #Error:missing"]
  }

  @Issue("https://github.com/spockframework/spock/issues/353")
  def "method name can act as naming pattern"() {
    when:
    def result = runner.runSpecBody("""
def "one #actor.details.name.size() two"() {
  expect: true

  where:
  actor = new Actor()
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["one #actor.details.name.size() two",
                                                                        "one 4 two"]
  }


  @Issue("https://github.com/spockframework/spock/issues/353")
  def "naming pattern in @Unroll annotation wins over naming pattern in method name"() {
    when:
    def result = runner.runSpecBody("""
@Unroll("#actor.details.name")
def "#actor.details.age"() {
  expect: true

  where:
  actor = new Actor()
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["#actor.details.age",
                                                                        "fred"]
  }

  @Issue("https://github.com/spockframework/spock/issues/354")
  def "method-level unroll annotation wins over class-level annotation"() {
    when:
    def result = runner.runWithImports("""
@Unroll("a feature")
class Foo extends Specification {
  @Unroll("#actor.details.name")
  def method() {
    expect: true

    where:
    actor = new Actor()
  }
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["method",
                                                                        "fred"]
  }

  def "method-level unroll annotation wins over class-level uproll annotation"() {
    when:
    def result = runner.runWithImports("""
@Rollup
class Foo extends Specification {
  @Unroll("#actor.details.name")
  def method() {
    expect: true

    where:
    actor = new Actor()
  }
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["method",
                                                                        "fred"]
  }

  def "method-level unroll annotation wins over configuration script"() {
    given:
    runner.configurationScript {
      unroll {
        unrollByDefault false
      }
    }

    when:
    def result = runner.runSpecBody("""
@Unroll("#actor.details.name")
def method() {
  expect: true

  where:
  actor = new Actor()
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["method",
                                                                        "fred"]
  }

  def "class-level unroll annotation wins over configuration script"() {
    given:
    runner.configurationScript {
      unroll {
        unrollByDefault false
      }
    }

    when:
    def result = runner.runWithImports("""
@Unroll("#actor.details.name")
class Foo extends Specification {
  def method() {
    expect: true

    where:
    actor = new Actor()
  }
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["method",
                                                                        "fred"]
  }

  def "unroll annotations are not inheritable"() {
    given:
    runner.configurationScript {
      unroll {
        unrollByDefault false
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
      "methodBase",
      "methodBase: fred",
      "methodSub"
    ]
  }

  @Issue("https://github.com/spockframework/spock/issues/390")
  def "method name can still contain parentheses"() {
    when:
    def result = runner.runSpecBody("""
def "an actor (named #actor.getName()) age #actor.getAge()"() {
  expect: true

  where:
  actor = new Actor2()
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["an actor (named #actor.getName()) age #actor.getAge()",
                                                                        "an actor (named fred) age 30"]
  }


  @Issue("https://github.com/spockframework/spock/issues/1236")
  def "custom name provider for both simple and parameterized features"() {
    given:
    runner.addClassImport(Suffixed)
    when:
    def result = runner.runWithImports("""
@Suffixed
class ExampleSpec extends Specification {

    def "test"() {
        expect:
        true
    }

    def "parameterized"() {
        expect:
        true

        where:
        t << [0, 1]
    }
}
    """)

    then:
    result.testEvents().started().list().testDescriptor.displayName == ["test [suffix]",
                                                                        "parameterized [suffix]",
                                                                        "parameterized [t: 0, #0] [suffix]",
                                                                        "parameterized [t: 1, #1] [suffix]"]
  }

  static class Actor {
    Map details = [name: "fred", age: 30]
  }

  static class Actor2 {
    String name = "fred"
    int age = 30
  }

  static class Root extends Specification {
    def methodRoot() {
      expect: true

      where:
      actor = new Actor()
    }
  }

  @Unroll
  static class Base extends Root {
    def methodBase() {
      expect: true

      where:
      actor = new Actor()
    }
  }
}

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE])
@Inherited
@ExtensionAnnotation(SuffixedExtension)
@interface Suffixed {
}
class SuffixedExtension implements IAnnotationDrivenExtension<Suffixed> {

  @Override
  void visitSpecAnnotation(Suffixed annotation, SpecInfo spec) {

    spec.features.each {
      feature ->
        feature.reportIterations = true
        def currentIterationNameProvider = feature.iterationNameProvider
        feature.iterationNameProvider = { IterationInfo it ->
          def defaultName = currentIterationNameProvider == null ? feature.name : currentIterationNameProvider.getName(it)
          defaultName + " [suffix]"
        }

        feature.displayName = feature.name  + " [suffix]"
    }
  }
}
