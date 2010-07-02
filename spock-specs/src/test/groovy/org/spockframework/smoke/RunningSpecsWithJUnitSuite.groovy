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

package org.spockframework.smoke

import org.junit.runners.Suite.SuiteClasses
import org.junit.runners.Suite
import org.junit.runner.RunWith

import spock.lang.*

@RunWith(Suite)
@SuiteClasses([MySpec1, MySpec2])
class RunningSpecsWithJUnitSuite {}

class MySpec1 extends Specification {
  def foo() {
    expect: true
  }
}

class MySpec2 extends Specification {
  def foo() {
    expect: true
  }
}
