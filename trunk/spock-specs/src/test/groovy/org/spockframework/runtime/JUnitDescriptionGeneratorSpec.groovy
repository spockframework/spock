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
  
package org.spockframework.runtime

import org.spockframework.EmbeddedSpecification

class JUnitDescriptionGeneratorSpec extends EmbeddedSpecification {
  def "description of derived spec covers features of both base and derived spec"() {
    def derived = compiler.compileWithImports("""
class Base extends Specification {
  def f1() {
    expect: true
  }
}

class Derived extends Base {
  def f2() {
    expect: false
  }
}
    """).find { it.name.endsWith("Derived") }

    def specInfo = new SpecInfoBuilder(derived).build()

    when:
    new JUnitDescriptionGenerator(specInfo).generate()

    then:
    specInfo.metadata.children.size == 2
    specInfo.metadata.children.collect { it.methodName } == ["f1", "f2"]
  }
}
