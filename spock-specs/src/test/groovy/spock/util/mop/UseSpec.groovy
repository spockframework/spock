/*
 * Copyright 2010 the original author or authors.
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

package spock.util.mop

import org.spockframework.runtime.model.parallel.ExecutionMode
import spock.lang.Specification

class UseOnMethods extends Specification {
  // can be applied to a fixture method
  @Use(StringExtensions)
  def setupSpec() {
    assert "foo".duplicate() == "foofoo"
  }

  @Use(StringExtensions)
  def "can be applied to a feature method"() {
    expect:
    "foo".duplicate() == "foofoo"
  }

  @Use(StringExtensions)
  def "sets feature execution mode to SAME_THREAD"() {
    expect:
    specificationContext.currentFeature.executionMode.get() == ExecutionMode.SAME_THREAD
  }

  @Use([StringExtensions, IntegerExtensions])
  def "can use multiple categories"() {
    expect:
    3.mul(4) == 12
    "foo".duplicate() == "foofoo"
  }

  @Use(StringExtensions)
  @Use(IntegerExtensions)
  def "can use multiple categories in multiple annotations"() {
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

  @Use(StringExtensions)
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

@Use(StringExtensions)
class UseOnClasses extends Specification {
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

  def "sets childExecutionMode to SAME_THREAD"() {
    expect: specificationContext.currentSpec.childExecutionMode.get() == ExecutionMode.SAME_THREAD
  }

  def helper() {
    assert "foo".duplicate() == "foofoo"
  }

  @Use(IntegerExtensions)
  def "can be combined with method-level category"() {
    expect:
    3.mul(4) == 12
    "foo".duplicate() == "foofoo"
  }
}

class StringExtensions {
  static String duplicate(String str) { str * 2 }
}

class IntegerExtensions {
  static mul(Integer n, Integer m) { n * m }
}
