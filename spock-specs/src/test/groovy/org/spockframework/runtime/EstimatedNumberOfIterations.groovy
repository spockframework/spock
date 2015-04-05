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

package org.spockframework.runtime

import org.junit.runners.model.RunnerScheduler
import spock.lang.Specification

class EstimatedNumberOfIterations extends Specification {
  def runner =  new ParameterizedSpecRunner(
          null,
          null,
          new Scheduler(new SequentialRunnerScheduler(), false))

  def "w/o data provider"() {
    expect: "estimation is 1"
      runner.estimateNumIterations(new Object[0]) == 1
  }

  def "w/ data provider that doesn't respond to size"() {
    expect: "estimation is 'unknown', represented as -1"
      runner.estimateNumIterations([1] as Object[]) == -1
  }

  def "w/ data provider that responds to size"() {
    expect: "estimation is size"
      runner.estimateNumIterations([[1, 2, 3]] as Object[]) == 3
  }

  def "w/ multiple data providers, all of which respond to size"() {
    expect: "estimation is minimum"
      runner.estimateNumIterations([[1], [1, 2], [1, 2, 3]] as Object[]) == 1
  }

  def "w/ multiple data providers, one of which doesn't respond to size"() {
  expect: "estimation is minimum of others"
    runner.estimateNumIterations([1, [1, 2], [1, 2, 3]] as Object[]) == 2
  }
}