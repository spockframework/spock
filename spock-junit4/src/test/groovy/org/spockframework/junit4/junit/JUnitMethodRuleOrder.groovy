/*
 * Copyright 2011 the original author or authors.
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

import org.junit.Rule
import org.junit.rules.MethodRule
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement

import spock.lang.Specification

class JUnitMethodRuleOrder extends Specification {
  List log = []
  @Rule LoggingRule rule1 = new LoggingRule(log: log, msg: "testRule1")
  @Rule LoggingRule rule2 = new LoggingRule(log: log, msg: "testRule2")

  def setup() {
    // expect: setup is called during base.evaluate()
    assert log == ["testRule2: before", "testRule1: before"]
  }

  def "rules declared later wrap around rules declared earlier"() {
    expect:
    log == ["testRule2: before", "testRule1: before"]
  }

  def cleanup() {
    // expect: cleanup is called before base.evaluate() leaves
    assert log == ["testRule2: before", "testRule1: before"]
  }

  static class LoggingRule implements MethodRule {
    List log
    String msg

    Statement apply(Statement base, FrameworkMethod method, Object target) {
      new Statement() {
        @Override
        void evaluate() {
          log << "$msg: before"
          base.evaluate()
          log << "$msg: after"
        }
      }
    }
  }
}
