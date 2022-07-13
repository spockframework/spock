/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.junit4.junit

import org.spockframework.junit4.JUnitDescriptionGenerator
import org.spockframework.runtime.SpecInfoBuilder
import spock.lang.Issue

class JUnitDescriptionGeneratorSpec extends JUnitBaseSpec {
  @Issue("https://github.com/spockframework/spock/issues/176")
  def "derived spec has correct Description"() {
    def derived = compiler.compileWithImports("""
class Base extends Specification {
  def f1() {
    expect: true
  }
}

class Derived extends Base {
  def f2() {
    expect: true
  }
}
    """).find { it.simpleName == "Derived" }

    def specInfo = new SpecInfoBuilder(derived).build()

    when:
    def desc = JUnitDescriptionGenerator.describeSpec(specInfo)

    then:
    desc.displayName == "apackage.Derived"
    desc.children.size() == 2
    desc.children.collect { it.methodName } == ["f1", "f2"]
    desc.children.collect { it.className } == ["apackage.Derived", "apackage.Derived"]
  }
}
