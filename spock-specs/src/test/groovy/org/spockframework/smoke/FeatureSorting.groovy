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

import org.junit.runner.Request
import org.junit.runner.Description

import org.spockframework.EmbeddedSpecification

import spock.lang.*

class FeatureSorting extends EmbeddedSpecification {
  @Shared
  List clazzes = compiler.compileWithImports("""
class Base extends Specification {
  def aaa() { expect: true }
  def bbb() { expect: true }
  def ccc() { expect: true }
}

class Derived extends Base {
  def abc() { expect: true }
  def bcd() { expect: true }
  def cde() { expect: true }
}
  """)

  @Shared
  Class base = clazzes.find { it.simpleName == "Base" }

  @Shared
  Class derived = clazzes.find { it.simpleName == "Derived" }

  def "features are initially sorted in declaration order"() {
    when:
    // need to open new context to guard against reordering from outside (OptimizeRunOrderExtension etc.)
    def description = runner.withNewContext {
      Request.aClass(clazz).runner.description
    }

    then:
    description.children*.methodName == methodOrder

    where:
    clazz   | methodOrder
    base    | ["aaa", "bbb", "ccc"]
    derived | ["aaa", "bbb", "ccc", "abc", "bcd", "cde"]
  }

  def "sort in lexicographical order"() {
    when:
    def description = runner.withNewContext {
      Request.aClass(clazz).sortWith(new LexicographicalComparator()).runner.description
    }

    then:
    description.children*.methodName == methodOrder

    where:
    clazz   | methodOrder
    base    | ["aaa", "bbb", "ccc"]
    derived | ["aaa", "abc", "bbb", "bcd", "ccc", "cde"]
  }

  def "sort in reverse lexicographical order"() {
    when:
    def description = runner.withNewContext {
      Request.aClass(clazz).sortWith(new ReverseLexicographicalComparator()).runner.description
    }

    then:
    description.children*.methodName == methodOrder

	  where:
    clazz   | methodOrder
    base    | ["ccc", "bbb", "aaa"]
    derived | ["cde", "ccc", "bcd", "bbb", "abc", "aaa"]
  }
}

private class LexicographicalComparator implements Comparator<Description> {
  int compare(Description d1, Description d2) {
    d1.methodName <=> d2.methodName
  }
}

private class ReverseLexicographicalComparator implements Comparator<Description> {
  int compare(Description d1, Description d2) {
    d2.methodName <=> d1.methodName
  }
}