
/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.smoke.extension

import org.junit.Rule
import org.junit.rules.TestName

import spock.lang.*

class RuleExtension extends JUnitRuleBaseSpec {
  @Issue("http://issues.spockframework.org/detail?id=98")
  def "rules can be defined in base specs"() {
    expect:
    name.methodName == "rules can be defined in base specs"
  }
}

abstract class JUnitRuleBaseSpec extends Specification {
  @Rule
  TestName name = new TestName()
}
