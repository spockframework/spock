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

package org.spockframework.runtime

import org.spockframework.runtime.model.*
import spock.config.RunnerConfiguration
import spock.lang.Specification

import org.junit.platform.engine.EngineExecutionListener

class EstimatedNumberOfIterations extends Specification {

  def context = new SpockExecutionContext(Stub(EngineExecutionListener))
    .withErrorInfoCollector(new ErrorInfoCollector())
    .withCurrentInstance(this)
    .withRunContext(Stub(RunContext) {
      getConfiguration(RunnerConfiguration) >> new RunnerConfiguration()
    })
  def factory = new DataIteratorFactory(Stub(IRunSupervisor))

  def "w/o data provider"() {
    given:
    FeatureInfo featureInfo = Stub {
      getDataProcessorMethod() >> methodInfoFor('dataProcessor1')
      getDataProviders() >> []
      getDataVariableMultiplicationsMethod() >> null
    }

    expect: "estimation is 1"
    factory.createFeatureDataIterator(context.withCurrentFeature(featureInfo)).estimatedNumIterations == 1
  }


  @DataProcessorMetadata(dataVariables = [])
  Object[] dataProcessor1(Object[] args) {
    return new Object[0]
  }

  def "w/ data provider that doesn't respond to size"() {
    given:
    FeatureInfo featureInfo = Stub {
      getDataProcessorMethod() >> methodInfoFor('dataProcessor2')
      getDataProviders() >> [new DataProviderInfo(dataVariables: ['a'], previousDataTableVariables: [], dataProviderMethod: methodInfoFor('dataProvider2'))]
      getDataVariableMultiplicationsMethod() >> null
    }

    expect: "estimation is 'unknown', represented as -1"
    factory.createFeatureDataIterator(context.withCurrentFeature(featureInfo)).estimatedNumIterations == -1
  }


  @DataProviderMetadata(line = -1, dataVariables = ['a'])
  Iterator<String> dataProvider2() {
    ['a'].iterator()
  }

  @DataProcessorMetadata(dataVariables = ['a'])
  Object[] dataProcessor2(Object[] args) {
    return args
  }

  def "w/ data provider that responds to size"() {
    given:
    FeatureInfo featureInfo = Stub {
      getDataProcessorMethod() >> methodInfoFor('dataProcessor3')
      getDataProviders() >> [new DataProviderInfo(dataVariables: ['a'], previousDataTableVariables: [], dataProviderMethod: methodInfoFor('dataProvider3'))]
      getDataVariableMultiplicationsMethod() >> null
    }
    expect: "estimation is size"
    factory.createFeatureDataIterator(context.withCurrentFeature(featureInfo)).estimatedNumIterations == 3
  }

  @DataProviderMetadata(line = -1, dataVariables = ['a'])
  List<String> dataProvider3() {
    ['a', 'b', 'c']
  }

  @DataProcessorMetadata(dataVariables = ['a'])
  Object[] dataProcessor3(Object[] args) {
    return args
  }


  def "w/ multiple data providers, all of which respond to size"() {
    given:
    FeatureInfo featureInfo = Stub {
      getDataProcessorMethod() >> methodInfoFor('dataProcessor4')
      getDataProviders() >> [
        new DataProviderInfo(dataVariables: ['a'], previousDataTableVariables: [], dataProviderMethod: methodInfoFor('dataProvider4_1')),
        new DataProviderInfo(dataVariables: ['b'], previousDataTableVariables: [], dataProviderMethod: methodInfoFor('dataProvider4_2')),
        new DataProviderInfo(dataVariables: ['c'], previousDataTableVariables: [], dataProviderMethod: methodInfoFor('dataProvider4_3'))
      ]
      getDataVariableMultiplicationsMethod() >> null
    }
    expect: "estimation is minimum"
    factory.createFeatureDataIterator(context.withCurrentFeature(featureInfo)).estimatedNumIterations == 1
  }


  @DataProviderMetadata(line = -1, dataVariables = ['a'])
  List<String> dataProvider4_1() {
    ['a']
  }

  @DataProviderMetadata(line = -1, dataVariables = ['b'])
  Range<Integer> dataProvider4_2() {
    (1..3)
  }

  @DataProviderMetadata(line = -1, dataVariables = ['c'])
  Iterable<Integer> dataProvider4_3() {
    [1, 2]
  }

  @DataProcessorMetadata(dataVariables = ['a', 'b', 'c'])
  Object[] dataProcessor4(Object[] args) {
    return args
  }


  def "w/ multiple data providers, one of which doesn't respond to size"() {

    given:
    FeatureInfo featureInfo = Stub {
      getDataProcessorMethod() >> methodInfoFor('dataProcessor5')
      getDataProviders() >> [
        new DataProviderInfo(dataVariables: ['a'], previousDataTableVariables: [], dataProviderMethod: methodInfoFor('dataProvider5_1')),
        new DataProviderInfo(dataVariables: ['b'], previousDataTableVariables: [], dataProviderMethod: methodInfoFor('dataProvider5_2')),
        new DataProviderInfo(dataVariables: ['c'], previousDataTableVariables: [], dataProviderMethod: methodInfoFor('dataProvider5_3'))
      ]
      getDataVariableMultiplicationsMethod() >> null
    }
    expect: "estimation is minimum of others"
    factory.createFeatureDataIterator(context.withCurrentFeature(featureInfo)).estimatedNumIterations == 2
  }


  @DataProviderMetadata(line = -1, dataVariables = ['a'])
  Iterator<String> dataProvider5_1() {
    ['a'].iterator()
  }

  @DataProviderMetadata(line = -1, dataVariables = ['b'])
  Range<Integer> dataProvider5_2() {
    (1..3)
  }

  @DataProviderMetadata(line = -1, dataVariables = ['c'])
  Iterable<Integer> dataProvider5_3() {
    [1, 2]
  }

  @DataProcessorMetadata(dataVariables = ['a', 'b', 'c'])
  Object[] dataProcessor5(Object[] args) {
    return args
  }

  private MethodInfo methodInfoFor(String methodName) {
    EstimatedNumberOfIterations.declaredMethods.find { it.name == methodName }.with { new MethodInfo(reflection: it) }
  }
}
