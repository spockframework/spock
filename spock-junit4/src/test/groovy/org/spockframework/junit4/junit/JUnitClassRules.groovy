/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.junit4.junit

import org.junit.ClassRule
import org.junit.rules.TestName
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.spockframework.runtime.InvalidSpecException
import spock.lang.Issue
import spock.lang.ResourceLock
import spock.lang.Shared

class JUnitClassRules extends JUnitBaseSpec {
  @ClassRule @Shared MyRule rule1
  @ClassRule @Shared MyRule rule2 = new MyRule(42)

  def setup() {
    runner.addClassImport(ClassRule)
    runner.addClassImport(TestName)
    runner.addClassImport(OrderTracker)
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
      @ClassRule @Shared testName = new TestName()
    """

    then:
    InvalidSpecException e = thrown()
    e.message.contains("does not have a declared type")
  }

  def "must declare a type that implements TestRule"() {
    when:
    runner.runSpecBody """
      @ClassRule @Shared String rule = "rule"
    """

    then:
    InvalidSpecException e = thrown()
    e.message.contains("does not appear to be a rule type")
  }

  def "must be @Shared"() {
    when:
    runner.runSpecBody """
      @ClassRule TestName testName = new TestName()
    """

    then:
    InvalidSpecException e = thrown()
    e.message.contains("must be @Shared")
  }

  @ResourceLock("OrderTracker")
  @Issue("https://github.com/spockframework/spock/issues/1050")
  def "rules from parent fields should run before rules in child specs"() {
    given:
    OrderTracker.invocations = []
    when:
    runner.runWithImports """
abstract class Parent extends Specification {
      @Shared @ClassRule OrderTracker parent1 = new OrderTracker("parent-1")
      @Shared @ClassRule OrderTracker parent2 = new OrderTracker("parent-2")

}
class Child extends Parent {
      @Shared @ClassRule OrderTracker child1 = new OrderTracker("child-1")
      @Shared @ClassRule OrderTracker child2 = new OrderTracker("child-2")

    def "test"() {
        expect: true
    }
}
    """

    then:
    OrderTracker.invocations == [
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

  static @ClassRule TestName staticRule

  def "static class rules are not detected"() {
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

  static class OrderTracker implements TestRule {
    static List<String> invocations = []

    private final String identifier

    OrderTracker(String identifier) {
      this.identifier = identifier
    }

    @Override
    Statement apply(Statement base, Description description) {
      new Statement() {
        @Override
        void evaluate() {
          invocations << "before $identifier".toString()
          base.evaluate()
          invocations << "after $identifier".toString()
        }
      }
    }
  }
}
