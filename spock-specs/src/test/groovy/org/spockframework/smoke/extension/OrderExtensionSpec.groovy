/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.extension

import groovy.transform.Canonical
import groovy.transform.ToString
import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.extension.builtin.OrderExtension
import org.spockframework.runtime.extension.builtin.orderer.AlphabeticalSpecOrderer
import org.spockframework.runtime.extension.builtin.orderer.AnnotatationBasedSpecOrderer
import org.spockframework.runtime.extension.builtin.orderer.RandomSpecOrderer
import spock.lang.Order
import spock.lang.Retry

import static spock.lang.Retry.Mode.SETUP_FEATURE_CLEANUP

class OrderExtensionSpec extends EmbeddedSpecification {

  @Canonical
  @ToString(includePackage = false)
  static class SpecExecution {
    String spec
    List<String> features = []
  }

  static ThreadLocal<List<SpecExecution>> executionsTL = new ThreadLocal<>()

  List<Class> specs

  def setup() {
    executionsTL.set([])
    compiler.addClassMemberImport(OrderExtensionSpec)
    compiler.addClassImport(Order)
    specs = compiler.compileWithImports("""
class FirstSpec extends Specification {
  @Shared execution = new SpecExecution(spec: this.class.simpleName)
  def setup() { execution.features << specificationContext.currentFeature.name }
  def cleanupSpec() { executionsTL.get() << execution }
  def one() { expect: true }
  def two() { expect: true }
  def three() { expect: true }
}

@Order(-1)
class SecondSpec extends Specification {
  @Shared execution = new SpecExecution(spec: this.class.simpleName)
  def setup() { execution.features << specificationContext.currentFeature.name }
  def cleanupSpec() { executionsTL.get() << execution }
  def foo() { expect: true }
  @Order(99) def bar() { expect: true }
  @Order(-5) def zot() { expect: true }
}

class ThirdSpec extends Specification {
  @Shared execution = new SpecExecution(spec: this.class.simpleName)
  def setup() { execution.features << specificationContext.currentFeature.name }
  def cleanupSpec() { executionsTL.get() << execution }
  def "some feature"() { expect: true }
  @Order(1) def "another feature"() { expect: true }
  def "one more feature"() { expect: true }
}

class FourthSpec extends Specification {
  @Shared execution = new SpecExecution(spec: this.class.simpleName)
  def setup() { execution.features << specificationContext.currentFeature.name }
  def cleanupSpec() { executionsTL.get() << execution }
  def 'feature X'() { expect: true }
  def 'feature M'() { expect: true }
  @Order(-1) def 'feature D'() { expect: true }
}
  """)
  }

  def 'default order'() {
    runner.configurationScript = {
      runner {}
    }
    runner.extensionClasses << OrderExtension

    when:
    runner.runClasses(specs)
    def executions = executionsTL.get()

    then:
    executions*.spec == ['FirstSpec', 'SecondSpec', 'ThirdSpec', 'FourthSpec']
    executions*.features == [
      ['one', 'two', 'three'],
      ['foo', 'bar', 'zot'],
      ['some feature', 'another feature', 'one more feature'],
      ['feature X', 'feature M', 'feature D']
    ]
  }

  @Retry(count = 50, mode = SETUP_FEATURE_CLEANUP)
  // if random order == deterministic order
  def 'random spec order'() {
    runner.configurationScript = {
      runner {
        orderer new RandomSpecOrderer(true, false)
      }
    }
    runner.extensionClasses << OrderExtension

    when:
    runner.runClasses(specs)
    def executions = executionsTL.get()

    then:
    executions*.spec != ['FirstSpec', 'SecondSpec', 'ThirdSpec', 'FourthSpec']
    executions*.features.containsAll([
      ['one', 'two', 'three'],
      ['foo', 'bar', 'zot'],
      ['some feature', 'another feature', 'one more feature'],
      ['feature X', 'feature M', 'feature D']
    ])
  }

  @Retry(count = 50, mode = SETUP_FEATURE_CLEANUP)
  // if random order == deterministic order
  def 'random feature order'() {
    runner.configurationScript = {
      runner {
        orderer new RandomSpecOrderer(false, true)
      }
    }
    runner.extensionClasses << OrderExtension

    when:
    runner.runClasses(specs)
    def executions = executionsTL.get()

    then:
    executions*.spec == ['FirstSpec', 'SecondSpec', 'ThirdSpec', 'FourthSpec']
    !executions*.features.containsAll([
      ['one', 'two', 'three'],
      ['foo', 'bar', 'zot'],
      ['some feature', 'another feature', 'one more feature'],
      ['feature X', 'feature M', 'feature D']
    ])
  }

  @Retry(count = 50, mode = SETUP_FEATURE_CLEANUP)
  // if random order == deterministic order
  def 'random spec and feature order'() {
    runner.configurationScript = {
      runner {
        orderer new RandomSpecOrderer(true, true)
      }
    }
    runner.extensionClasses << OrderExtension

    when:
    runner.runClasses(specs)
    def executions = executionsTL.get()

    then:
    executions*.spec != ['FirstSpec', 'SecondSpec', 'ThirdSpec', 'FourthSpec']
    !executions*.features.containsAll([
      ['one', 'two', 'three'],
      ['foo', 'bar', 'zot'],
      ['some feature', 'another feature', 'one more feature'],
      ['feature X', 'feature M', 'feature D']
    ])
  }

  def 'repeatable, random-seeded spec and feature order'() {
    runner.configurationScript = {
      runner {
        orderer new RandomSpecOrderer(true, true, 42L)
      }
    }
    runner.extensionClasses << OrderExtension

    when:
    runner.runClasses(specs)
    def executions = executionsTL.get()

    then:
    executions*.spec == ['SecondSpec', 'ThirdSpec', 'FourthSpec', 'FirstSpec']
    executions*.features == [
      ['bar', 'zot', 'foo'],
      ['another feature', 'one more feature', 'some feature'],
      ['feature M', 'feature X', 'feature D'],
      ['one', 'two', 'three']
    ]
  }

  def 'alphabetical spec order'() {
    runner.configurationScript = {
      runner {
        orderer new AlphabeticalSpecOrderer(true, false)
      }
    }
    runner.extensionClasses << OrderExtension

    when:
    runner.runClasses(specs)
    def executions = executionsTL.get()

    then:
    executions*.spec == ['FirstSpec', 'FourthSpec', 'SecondSpec', 'ThirdSpec']
    executions*.features == [
      ['one', 'two', 'three'],
      ['feature X', 'feature M', 'feature D'],
      ['foo', 'bar', 'zot'],
      ['some feature', 'another feature', 'one more feature']
    ]
  }

  def 'alphabetical feature order'() {
    runner.configurationScript = {
      runner {
        orderer new AlphabeticalSpecOrderer(false, true)
      }
    }
    runner.extensionClasses << OrderExtension

    when:
    runner.runClasses(specs)
    def executions = executionsTL.get()

    then:
    executions*.spec == ['FirstSpec', 'SecondSpec', 'ThirdSpec', 'FourthSpec']
    executions*.features == [
      ['one', 'three', 'two'],
      ['bar', 'foo', 'zot'],
      ['another feature', 'one more feature', 'some feature'],
      ['feature D', 'feature M', 'feature X']
    ]
  }

  def 'alphabetical spec and feature order'() {
    runner.configurationScript = {
      runner {
        orderer new AlphabeticalSpecOrderer(true, true)
      }
    }
    runner.extensionClasses << OrderExtension

    when:
    runner.runClasses(specs)
    def executions = executionsTL.get()

    then:
    executions*.spec == ['FirstSpec', 'FourthSpec', 'SecondSpec', 'ThirdSpec']
    executions*.features == [
      ['one', 'three', 'two'],
      ['feature D', 'feature M', 'feature X'],
      ['bar', 'foo', 'zot'],
      ['another feature', 'one more feature', 'some feature']
    ]
  }

  def 'descending, alphabetical spec and feature order'() {
    runner.configurationScript = {
      runner {
        orderer new AlphabeticalSpecOrderer(true, true, true)
      }
    }
    runner.extensionClasses << OrderExtension

    when:
    runner.runClasses(specs)
    def executions = executionsTL.get()

    then:
    executions*.spec == ['FirstSpec', 'FourthSpec', 'SecondSpec', 'ThirdSpec'].reverse()
    executions*.features == [
      ['one', 'three', 'two'].reverse(),
      ['feature D', 'feature M', 'feature X'].reverse(),
      ['bar', 'foo', 'zot'].reverse(),
      ['another feature', 'one more feature', 'some feature'].reverse()
    ].reverse()
  }

  def 'annotation-based spec and feature order'() {
    runner.configurationScript = {
      runner {
        orderer new AnnotatationBasedSpecOrderer()
      }
    }
    runner.extensionClasses << OrderExtension

    when:
    runner.runClasses(specs)
    def executions = executionsTL.get()

    then:
    executions*.spec == ['SecondSpec', 'FirstSpec', 'ThirdSpec', 'FourthSpec']
    executions*.features == [
      ['zot', 'foo', 'bar'],
      ['one', 'two', 'three'],
      ['some feature', 'one more feature', 'another feature'],
      ['feature D', 'feature X', 'feature M']
    ]
  }

}
