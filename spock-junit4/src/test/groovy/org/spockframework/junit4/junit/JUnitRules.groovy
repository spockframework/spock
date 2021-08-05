/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.junit4.junit

import org.spockframework.runtime.InvalidSpecException

import org.junit.*
import org.junit.rules.*
import org.junit.runner.Description
import org.junit.runners.model.Statement
import spock.lang.Issue
import spock.lang.ResourceLock

class JUnitRules extends JUnitBaseSpec {
  @Rule
  MyRule rule1
  @Rule
  MyRule rule2 = new MyRule(42)

  def setup() {
    runner.addClassImport(Rule)
    runner.addClassImport(TestName)
    runner.addClassImport(AbortRule)
    runner.addClassImport(JUnitClassRules.OrderTracker)
  }

  def "are instantiated automatically by default"() {
    expect:
    rule1 != null
    rule1.num == 0
  }

  def "can be instantiated manually"() {
    expect:
    rule2 != null
    rule2.num == 42
  }

  def "must declare a type"() {
    when:
    runner.runSpecBody """
      @Rule testName = new TestName()
    """

    then:
    InvalidSpecException e = thrown()
    e.message.contains("does not have a declared type")
  }

  def "must declare a type that implements MethodRule or TestRule"() {
    when:
    runner.runSpecBody """
      @Rule String rule = "rule"
    """

    then:
    InvalidSpecException e = thrown()
    e.message.contains("does not appear to be a rule type")
  }

  def "cannot be @Shared"() {
    when:
    runner.runSpecBody """
      @Rule @Shared TestName testName = new TestName()
    """

    then:
    InvalidSpecException e = thrown()
    e.message.contains("cannot be @Shared")
  }

  def "exceptions in rules are translated"() {
    given:
    runner.throwFailure = false

    when:
    def result = runner.runSpecBody '''
      @Rule
      AbortRule skipRule

      def "feature"() {
      expect: false
      }
'''
    then:
    result.testsStartedCount == 1
    result.testsFailedCount == 0
    result.testsAbortedCount == 1
  }

  @ResourceLock("OrderTracker")
  @Issue("https://github.com/spockframework/spock/issues/1050")
  def "rules from parent fields should run before rules in child specs"() {
    given:
    JUnitClassRules.OrderTracker.invocations = []
    when:
    runner.runWithImports """
abstract class Parent extends Specification {
      @Rule OrderTracker parent1 = new OrderTracker("parent-1")
      @Rule OrderTracker parent2 = new OrderTracker("parent-2")

}
class Child extends Parent {
      @Rule OrderTracker child1 = new OrderTracker("child-1")
      @Rule OrderTracker child2 = new OrderTracker("child-2")

    def "test"() {
        expect: true
    }
}
    """

    then:
    JUnitClassRules.OrderTracker.invocations == [
      "before parent-1",
      "before parent-2",
      "before child-1",
      "before child-2",
      "after child-2",
      "after child-1",
      "after parent-2",
      "after parent-1",
    ]
  }

  static @Rule
  TestName staticRule

  def "static rules are not detected"() {
    expect:
    staticRule == null
  }

  static class MyRule implements TestRule {
    int num = 0

    MyRule() {}

    MyRule(int num) {
      this.num = num
    }

    Statement apply(Statement base, Description description) {
      new Statement() {
        @Override
        void evaluate() {
          base.evaluate()
        }
      }
    }
  }

  static class AbortRule implements TestRule {

    Statement apply(Statement base, Description description) {
      new Statement() {
        @Override
        void evaluate() {
          Assume.assumeTrue(false)
        }
      }
    }
  }
}
