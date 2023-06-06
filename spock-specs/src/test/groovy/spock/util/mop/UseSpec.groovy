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

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.model.parallel.ExecutionMode
import spock.lang.Isolated
import spock.lang.Requires
import spock.lang.Specification

import java.util.concurrent.CountDownLatch

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

@Isolated("Isolate from other tests to have full access to cores for the embedded tests.")
@Requires({ Runtime.runtime.availableProcessors() >= 2 })
class IsolatedUseSpec extends EmbeddedSpecification {
  def setup() {
    runner.addClassImport(Use)
    runner.addClassImport(CountDownLatch)
    runner.addClassImport(StringExtensions)
    runner.addClassImport(ExecutionMode)
    runner.configurationScript {
      runner {
        parallel {
          enabled true
          fixed(4)
        }
      }
    }
  }

  def "executing iterations in parallel works with annotated specification if enabled"() {
    when:
    getSpecificationContext()
    def result = runner.runWithImports '''
      @Use(StringExtensions)
      class ASpec extends Specification {
        @Shared
        CountDownLatch latch = new CountDownLatch(2)

        @IgnoreIf({ instance.specificationContext.currentSpec.childExecutionMode.orElse(null) == ExecutionMode.SAME_THREAD })
        def feature() {
          given:
          latch.countDown()
          latch.await()

          expect:
          "foo".duplicate() == "foofoo"

          where:
          i << (1..2)
        }
      }
    '''

    then:
    result.testsSucceededCount + result.testsAbortedCount == 3
  }

  def "executing iterations in parallel works with annotated feature"() {
    when:
    getSpecificationContext()
    def result = runner.runSpecBody '''
      @Shared
      CountDownLatch latch = new CountDownLatch(2)

      @Use(StringExtensions)
      def feature() {
        given:
        latch.countDown()
        latch.await()

        expect:
        "foo".duplicate() == "foofoo"

        where:
        i << (1..2)
      }
    '''

    then:
    result.testsSucceededCount + result.testsAbortedCount == 3
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
