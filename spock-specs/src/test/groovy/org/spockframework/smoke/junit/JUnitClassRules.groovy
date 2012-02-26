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

import org.junit.rules.TestName
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.junit.ClassRule

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.InvalidSpecException

import spock.lang.Shared

class JUnitClassRules extends EmbeddedSpecification {
  @ClassRule @Shared MyRule rule1
  @ClassRule @Shared MyRule rule2 = new MyRule(42)

  def setup() {
    runner.addClassImport(ClassRule)
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
}
