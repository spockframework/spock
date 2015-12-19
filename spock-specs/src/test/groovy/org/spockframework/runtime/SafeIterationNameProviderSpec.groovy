/*
 * Copyright 2012 the original author or authors.
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

import org.spockframework.runtime.model.NameProvider
import org.spockframework.runtime.model.IterationInfo
import org.spockframework.runtime.model.FeatureInfo

import spock.lang.Specification

class SafeIterationNameProviderSpec extends Specification {
  def feature = new FeatureInfo()
  def iteration = new IterationInfo(feature, new HashMap<>(), 3)
  def other = Mock(NameProvider)
  def provider = new SafeIterationNameProvider(other)

  def setup() {
    feature.name = "feature"
  }

  def "delegates to other provider"() {
    when:
    provider.getName(iteration)

    then:
    1 * other.getName(iteration)
  }

  def "returns default if there is no other provider"() {
    provider = new SafeIterationNameProvider(null)

    expect:
    provider.getName(iteration) == "feature"
  }

  def "returns default if other provider returns nothing"() {
    other.getName(iteration) >> null

    expect:
    provider.getName(iteration) == "feature"
  }

  def "returns default if other provider blows up"() {
    other.getName(iteration) >> { throw new RuntimeException() }

    expect:
    provider.getName(iteration) == "feature"
  }

  def "iteration name defaults to feature name when iterations aren't reported"() {
    feature.reportIterations = false

    expect:
    provider.getName(iteration) == "feature"
    provider.getName(iteration) == "feature"
    provider.getName(iteration) == "feature"
  }

  def "iteration name defaults to indexed feature name when iterations are reported"() {
    feature.reportIterations = true

    expect:
    provider.getName(iteration) == "feature[0]"
    provider.getName(iteration) == "feature[1]"
    provider.getName(iteration) == "feature[2]"
  }
}
