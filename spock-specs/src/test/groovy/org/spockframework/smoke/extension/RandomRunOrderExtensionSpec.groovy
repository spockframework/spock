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
import org.spockframework.runtime.extension.builtin.RandomRunOrderExtension
import spock.lang.Retry

import static spock.lang.Retry.Mode.SETUP_FEATURE_CLEANUP

class RandomRunOrderExtensionSpec extends EmbeddedSpecification {
  static ThreadLocal<List<SpecExecution>> executionsTL = new ThreadLocal<>()

  List<Class> specs

  def setup() {
    executionsTL.set([])
    compiler.addClassMemberImport(RandomRunOrderExtensionSpec)
    specs = compiler.compileWithImports("""
class FirstSpec extends Specification {
  @Shared execution = new SpecExecution(spec: this.class.simpleName)
  def setup() { execution.features << specificationContext.currentFeature.name }
  def cleanupSpec() { executionsTL.get() << execution }
  def one() { expect: true }
  def two() { expect: true }
  def three() { expect: true }
}

class SecondSpec extends Specification {
  @Shared execution = new SpecExecution(spec: this.class.simpleName)
  def setup() { execution.features << specificationContext.currentFeature.name }
  def cleanupSpec() { executionsTL.get() << execution }
  def one() { expect: true }
  def two() { expect: true }
  def three() { expect: true }
}

class ThirdSpec extends Specification {
  @Shared execution = new SpecExecution(spec: this.class.simpleName)
  def setup() { execution.features << specificationContext.currentFeature.name }
  def cleanupSpec() { executionsTL.get() << execution }
  def one() { expect: true }
  def two() { expect: true }
  def three() { expect: true }
}
  """)
  }

  def "deterministic default order"() {
    runner.configurationScript = {
      runner {}
    }
    runner.extensionClasses << RandomRunOrderExtension

    when:
    runner.runClasses(specs)
    def executions = executionsTL.get()

    then:
    executions*.spec == ['FirstSpec', 'SecondSpec', 'ThirdSpec']
    executions*.features == [['one', 'two', 'three'], ['one', 'two', 'three'], ['one', 'two', 'three']]
  }

  @Retry(count = 50, mode = SETUP_FEATURE_CLEANUP)  // if random order == deterministic order
  def "random spec order"() {
    runner.configurationScript = {
      runner {
        randomizeSpecRunOrder true
        randomizeFeatureRunOrder false
      }
    }
    runner.extensionClasses << RandomRunOrderExtension

    when:
    runner.runClasses(specs)
    def executions = executionsTL.get()

    then:
    executions*.spec != ['FirstSpec', 'SecondSpec', 'ThirdSpec']
    executions*.features == [['one', 'two', 'three'], ['one', 'two', 'three'], ['one', 'two', 'three']]
  }

  @Retry(count = 50, mode = SETUP_FEATURE_CLEANUP)  // if random order == deterministic order
  def "random feature order"() {
    runner.configurationScript = {
      runner {
        randomizeSpecRunOrder false
        randomizeFeatureRunOrder true
      }
    }
    runner.extensionClasses << RandomRunOrderExtension

    when:
    runner.runClasses(specs)
    def executions = executionsTL.get()

    then:
    executions*.spec == ['FirstSpec', 'SecondSpec', 'ThirdSpec']
    executions*.features != [['one', 'two', 'three'], ['one', 'two', 'three'], ['one', 'two', 'three']]
  }

  @Retry(count = 50, mode = SETUP_FEATURE_CLEANUP)  // if random order == deterministic order
  def "random feature and spec order"() {
    runner.configurationScript = {
      runner {
        randomizeSpecRunOrder true
        randomizeFeatureRunOrder true
      }
    }
    runner.extensionClasses << RandomRunOrderExtension

    when:
    runner.runClasses(specs)
    def executions = executionsTL.get()

    then:
    executions*.spec != ['FirstSpec', 'SecondSpec', 'ThirdSpec']
    executions*.features != [['one', 'two', 'three'], ['one', 'two', 'three'], ['one', 'two', 'three']]
  }

  @Canonical
  @ToString(includePackage = false)
  static class SpecExecution {
    String spec
    List<String> features = []
  }

}
