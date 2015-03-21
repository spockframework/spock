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

import org.junit.runner.notification.RunListener

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.SpockExecutionException

import spock.lang.Issue

/**
 * @author Peter Niederwieser
 */
class UnrolledFeatureMethods extends EmbeddedSpecification {
  RunListener listener = Mock()

  def setup() {
    runner.listeners << listener
    runner.addClassImport(Actor)
    runner.addClassImport(Actor2)
  }

  def "iterations of an unrolled feature count as separate tests"() {
    when:
    def result = runner.runSpecBody("""
@Unroll
def foo() {
  expect: true

  where:
  x << [1, 2, 3]
}
    """)

    then:
    result.runCount == 3
    result.failureCount == 0
    result.ignoreCount == 0
  }

  def "iterations of an unrolled feature foo are named foo[0], foo[1], etc."() {
    when:
    runner.runSpecBody """
@Unroll
def foo() {
  expect: true

  where:
  x << [1, 2, 3]
}
    """

    then:
    interaction {
      def count = 0
      3 * listener.testStarted { it.methodName == "foo[${count++}]" }
    }
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
    annotation << ["", "@Unroll"]
  }

  def "if creation of a data provider fails, feature isn't unrolled"() {
    runner.throwFailure = false

    when:
    def result = runner.runSpecBody("""
@Unroll
def foo() {
  expect: true

  where:
  x << [1]
  y << { throw new Exception() }()
}
    """)

    then:
    1 * listener.testFailure { it.description.methodName == "foo" }

    // it's OK for runCount to be zero here; mental model: method "foo"
    // would have been invisible if it had not failed (cf. Spec JUnitErrorBehavior)
    result.runCount == 0
    result.failureCount == 1
    result.ignoreCount == 0
  }

  def "naming pattern may refer to data variables"() {
    when:
    runner.runSpecBody("""
@Unroll("one #y two #x three")
def foo() {
  expect: true

  where:
  x << [1, 2, 3]
  y << ["a", "b", "c"]
}
    """)

    then:
    1 * listener.testStarted { it.methodName == "one a two 1 three" }
    1 * listener.testStarted { it.methodName == "one b two 2 three" }
    1 * listener.testStarted { it.methodName == "one c two 3 three" }
  }

  def "naming pattern may refer to feature name and iteration count"() {
    when:
    runner.runSpecBody("""
@Unroll("one #featureName two #iterationCount three")
def foo() {
  expect: true

  where:
  x << [1, 2, 3]
  y << ["a", "b", "c"]
}
    """)

    then:
    1 * listener.testStarted { it.methodName == "one foo two 0 three" }
    1 * listener.testStarted { it.methodName == "one foo two 1 three" }
    1 * listener.testStarted { it.methodName == "one foo two 2 three" }
  }

  @Issue("http://issues.spockframework.org/detail?id=65")
  def "variables in naming pattern whose value is null are replaced correctly"() {
    when:
    runner.runSpecBody("""
@Unroll("one #x two #y three")
def foo() {
  expect: true

  where:
  x << [1]
  y << [null]
}
    """)

    then:
    1 * listener.testStarted { it.methodName == "one 1 two null three" }
  }

  @Issue("http://issues.spockframework.org/detail?id=231")
  def "naming pattern supports property expressions"() {
    when:
    runner.runSpecBody("""
@Unroll("one #actor.details.name two")
def foo() {
  expect: true

  where:
  actor = new Actor()
}
    """)

    then:
    1 * listener.testStarted { it.methodName == "one fred two" }
  }

  @Issue("http://issues.spockframework.org/detail?id=231")
  def "naming pattern supports zero-arg method calls"() {
    when:
    runner.runSpecBody("""
@Unroll("one #actor.details.name.size() two")
def foo() {
  expect: true

  where:
  actor = new Actor()
}
    """)

    then:
    1 * listener.testStarted { it.methodName == "one 4 two" }
  }

  def "expressions in naming pattern that can't be evaluated are prefixed with 'Error:'"() {
    when:
    runner.runSpecBody("""
@Unroll("#obj #obj.ok() #obj.bang() #obj.missing() #missing")
def foo() {
  expect: true

  where:
  obj = { def obj = new Expando(); obj.ok = { "ok" }; obj.toString = { throw new RuntimeException() }; obj.bang = { throw new NullPointerException() }; obj }()
}
    """)

    then:
    1 * listener.testStarted { it.methodName == "#Error:obj ok #Error:obj.bang() #Error:obj.missing() #Error:missing" }
  }

  @Issue("http://issues.spockframework.org/detail?id=231")
  def "method name can act as naming pattern"() {
    when:
    runner.runSpecBody("""
@Unroll
def "one #actor.details.name.size() two"() {
  expect: true

  where:
  actor = new Actor()
}
    """)

    then:
    1 * listener.testStarted { it.methodName == "one 4 two" }
  }


  @Issue("http://issues.spockframework.org/detail?id=231")
  def "naming pattern in @Unroll annotation wins over naming pattern in method name"() {
    when:
    runner.runSpecBody("""
@Unroll("#actor.details.name")
def "#actor.details.age"() {
  expect: true

  where:
  actor = new Actor()
}
    """)

    then:
    1 * listener.testStarted { it.methodName == "fred" }
  }

  @Issue("http://issues.spockframework.org/detail?id=232")
  def "can unroll a whole class at once"() {
    when:
    runner.runWithImports("""
@Unroll
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
    1 * listener.testStarted { it.methodName == "fred" }
    1 * listener.testStarted { it.methodName == "not data-driven" }
    1 * listener.testStarted { it.methodName == "30" }
  }

  @Issue("http://issues.spockframework.org/detail?id=232")
  def "method-level unroll annotation wins over class-level annotation"() {
    when:
    runner.runWithImports("""
@Unroll
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
    1 * listener.testStarted { it.methodName == "fred" }
  }

  @Issue("http://issues.spockframework.org/detail?id=268")
  def "method name can still contain parentheses"() {
    when:
    runner.runSpecBody("""
@Unroll
def "an actor (named #actor.getName()) age #actor.getAge()"() {
  expect: true

  where:
  actor = new Actor2()
}
    """)

    then:
    1 * listener.testStarted { it.methodName == "an actor (named fred) age 30" }

  }

  def "duplicated generated names should become different"() {
    def List<String> descriptions = new ArrayList<>()
    when:
    runner.runSpecBody("""
@Unroll
def "test #x"(int x) {
  expect: true

  where:
    x | _
    1 | _
    1 | _
    1 | _
    2 | _
    2 | _
}
    """)

    then:
     _ * listener.testStarted { descriptions.add(it.methodName) }
     descriptions.size() == new HashSet(descriptions).size() // no duplicates

  }



  static class Actor {
    Map details = [name: "fred", age: 30]
  }

  static class Actor2 {
    String name = "fred"
    int age = 30
  }


}