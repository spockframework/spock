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

package org.spockframework.smoke.extension

import spock.lang.Specification
import spock.lang.Use

class UseExtensionOnMethods extends Specification {
  // can be applied to a fixture method
  @Use(StringCategory)
  def setupSpec() {
    assert "foo".duplicate() == "foofoo"
  }

  @Use(StringCategory)
  def "can be applied to a feature method"() {
    expect:
    "foo".duplicate() == "foofoo"
  }

  @Use([StringCategory, IntCategory])
  def "can use multiple categories"() {
    expect:
    3.mul(4) == 12
    "foo".duplicate() == "foofoo"
  }

  def "has no effect when applied to a helper method"() {
    when:
    helper()

    then:
    thrown(MissingMethodException)
  }

  @Use(StringCategory)
  def helper() {
    "foo".duplicate()
  }

  def "has no effect on remaining feature methods"() {
    when:
    "foo".duplicate()

    then:
    thrown(MissingMethodException)
  }

  // has no effect on remaining fixture methods
  def setup() {
    try {
      "foo".duplicate()
      assert false
    } catch (MissingMethodException expected) {}
  }
}

@Use(StringCategory)
class UseExtensionOnClasses extends Specification {
  // affects fixture methods
  def setupSpec() {
    assert "foo".duplicate() == "foofoo"
  }

  def "affects feature methods"() {
    expect:
    "foo".duplicate() == "foofoo"
  }

  def "affects helper methods"() {
    when:
    helper()

    then:
    noExceptionThrown()
  }

  def helper() {
    assert "foo".duplicate() == "foofoo"
  }

  @Use(IntCategory)
  def "can be combined with method-level category"() {
    expect:
    3.mul(4) == 12
    "foo".duplicate() == "foofoo"
  }
}

class StringCategory {
  static String duplicate(String str) { str * 2 }
}

class IntCategory {
  static mul(Integer n, Integer m) { n * m }
}