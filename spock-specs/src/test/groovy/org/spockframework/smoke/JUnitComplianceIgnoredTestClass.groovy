
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

import org.junit.Test
import org.junit.runner.Request

import spock.lang.Specification
import spock.lang.Issue

@Issue("http://issues.spockframework.org/detail?id=77")
class JUnitComplianceIgnoredTestClass extends org.spockframework.EmbeddedSpecification {
  def "a priori description of ignored test class has no method descriptions"() {
    Request request = Request.aClass(base)
    def desc = request.runner.description

    expect:
    desc.className == base.name
    desc.displayName == base.name
    desc.children.size() == 0

    where:
    base << [Bar, BarSpec]
  }
}

class Foo {
  @Test
  void m1() {}
}

@org.junit.Ignore
class Bar extends Foo {
  @Test
  void m2() {}
}

class FooSpec extends Specification {
  def m1() { expect: true }
}

@spock.lang.Ignore
class BarSpec extends FooSpec {
  def m2() { expect: true }
}