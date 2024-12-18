/*
 * Copyright 2024 the original author or authors.
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

package org.spockframework.datapipes

import org.spockframework.EmbeddedSpecification
import spock.lang.Issue
import spock.util.EmbeddedSpecRunner

import static java.util.Objects.requireNonNull

class DataPipesIteratorSpec extends EmbeddedSpecification {

  private static final ThreadLocal<Object> currentDataProvider = new ThreadLocal<>()

  def cleanup() {
    currentDataProvider.remove()
  }

  def "Collection data provider will use size method to estimate number of iterations"() {
    given:
    def dataCollection = dataProvider(new TestDataCollection())

    when:
    def res = runIterations()

    then:
    res.testsSucceededCount == 2
    dataCollection.iteratorCalls == 1
    dataCollection.sizeCalls == 1
  }

  @Issue("https://github.com/spockframework/spock/issues/2022")
  def "Iterable uses the Groovy default size method to estimate number of iterations"() {
    given:
    def dataIterable = dataProvider(new TestDataIterable())

    when:
    def res = runIterations()

    then:
    dataIterable.iteratorCalls == 2
    res.testsSucceededCount == 2
  }

  @Issue("https://github.com/spockframework/spock/issues/2022")
  def "Iterable with size method shall use the size method to estimate number of iterations"() {
    given:
    def dataIterable = dataProvider(new TestDataIterableWithSize())

    when:
    def res = runIterations()

    then:
    res.testsSucceededCount == 2
    dataIterable.iteratorCalls == 1
    dataIterable.sizeCalls == 1
  }

  @Issue("https://github.com/spockframework/spock/issues/2022")
  def "Iterator shall be only called once"() {
    given:
    def dataIterator = dataProvider(new TestDataIterator())

    when:
    def res = runIterations()

    then:
    res.testsSucceededCount == 2
    dataIterator.hasNextCalls == 3
    dataIterator.nextCalls == 1
  }

  private EmbeddedSpecRunner.SummarizedEngineExecutionResults runIterations() {
    runner.runFeatureBody """
    expect:
    input != null

    where:
    //noinspection UnnecessaryQualifiedReference
    input << org.spockframework.datapipes.DataPipesIteratorSpec.getDataProvider()
"""
  }

  private static <T> T dataProvider(T dataProvider) {
    currentDataProvider.set(requireNonNull(dataProvider))
    return dataProvider
  }

  static Object getDataProvider() {
    def data = currentDataProvider.get()
    assert data != null
    return data
  }

  private static class TestDataCollection extends AbstractCollection {
    int iteratorCalls = 0
    int sizeCalls = 0

    @Override
    Iterator iterator() {
      iteratorCalls++
      return ["Value"].iterator()
    }

    @Override
    int size() {
      sizeCalls++
      return 1
    }
  }

  private static class TestDataIterableWithSize implements Iterable {
    int iteratorCalls = 0
    int sizeCalls = 0

    int size() {
      sizeCalls++
      return 1
    }

    @Override
    Iterator iterator() {
      iteratorCalls++
      return ["Value"].iterator()
    }
  }

  private static class TestDataIterable implements Iterable {
    int iteratorCalls = 0

    @Override
    Iterator iterator() {
      iteratorCalls++
      return ["Value"].iterator()
    }
  }

  private static class TestDataIterator implements Iterator {
    int hasNextCalls = 0
    int nextCalls = 0

    @Override
    boolean hasNext() {
      hasNextCalls++
      if (nextCalls == 0) {
        return true
      }
      return false
    }

    @Override
    Object next() {
      nextCalls++
      if (nextCalls == 1) {
        return "Value"
      }
      throw new NoSuchElementException()
    }
  }
}
