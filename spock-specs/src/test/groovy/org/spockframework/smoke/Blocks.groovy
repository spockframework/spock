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

package org.spockframework.smoke

import org.spockframework.EmbeddedSpecification

import org.spockframework.compiler.InvalidSpecCompileException
import static org.spockframework.runtime.model.BlockKind.*
import org.spockframework.runtime.RunContext
import org.spockframework.runtime.SpecInfoBuilder

class Blocks extends EmbeddedSpecification {
  def "labels and comments"() {
    def specClass = compiler.compileSpecBody("""
def m1() {
  setup: "setup"
  and: "setup2"
  when: "when"
  and: "when2"
  then: "then"
  and: "then2"
  where: "where"
  and: "where2"
}

def m1b() {
  setup: "setup"
  but: "setup2"
  when: "when"
  but: "when2"
  then: "then"
  but: "then2"
  where: "where"
  but: "where2"
}

def m2() {
  expect: "expect"
  and: "expect2"
}

def m2b() {
  expect: "expect"
  but: "expect2"
}

def m3() {
  given: "given"
  and: def x
  expect: def y
  and: "and"
  where: ""
  and: z << 1
}

def m3b() {
  given: "given"
  but: def x
  expect: def y
  but: "but"
  where: ""
  but: z << 1
}
    """)

    def specInfo = new SpecInfoBuilder(specClass).build()

    expect:
    def m1 = specInfo.features[0]
    m1.blocks*.kind == [SETUP,WHEN,THEN,WHERE]
    m1.blocks*.texts.flatten() == ["setup","setup2","when","when2","then","then2","where","where2"]


    and:
    def m1b = specInfo.features[1]
    m1b.blocks*.kind == [SETUP,WHEN,THEN,WHERE]
    m1b.blocks*.texts.flatten() == ["setup","setup2","when","when2","then","then2","where","where2"]

    and:
    def m2 = specInfo.features[2]
    m2.blocks*.kind == [EXPECT]
    m2.blocks*.texts.flatten() == ["expect","expect2"]

    and:
    def m2b = specInfo.features[3]
    m2b.blocks*.kind == [EXPECT]
    m2b.blocks*.texts.flatten() == ["expect","expect2"]

    and:
    def m3 = specInfo.features[4]
    m3.blocks*.kind == [SETUP,EXPECT,WHERE]
    m3.blocks*.texts.flatten() == ["given","and",""]

    and:
    def m3b = specInfo.features[5]
    m3b.blocks*.kind == [SETUP,EXPECT,WHERE]
    m3b.blocks*.texts.flatten() == ["given","but",""]
  }

  def "unknown label"() {
    when:
    compiler.compileFeatureBody("""
setuppp: def a = 1
    """)

    then:
    InvalidSpecCompileException e = thrown()
    e.line == 1
  }
}