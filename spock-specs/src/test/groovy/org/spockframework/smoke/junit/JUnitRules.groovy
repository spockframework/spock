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

package org.spockframework.smoke.junit

import org.junit.Rule

import org.junit.rules.TestRule
import org.junit.runners.model.Statement
import org.junit.runner.Description
import org.junit.rules.TestName

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.InvalidSpecException

class JUnitRules extends EmbeddedSpecification {
  @Rule MyRule rule1
  @Rule MyRule rule2 = new MyRule(42)

  def setup() {
    runner.addClassImport(Rule)
    runner.addClassImport(TestName)
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

  static @Rule TestName staticRule

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
}
