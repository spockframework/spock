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

package org.spockframework.example

import spock.lang.FeatureInfo
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Peter Niederwieser
 */
class FeatureUnrolling extends Specification {
  @FeatureInfo(id = "4b873cc6-f204-4517-aa3a-93834963046c",impact = 6,frequencyOfFailure = 1,component = "Feature Unrolling")
  def "without unrolling"() {
    expect:
    name.size() == length

    where:
    name << ["Kirk", "Spock", "Scotty"]
    length << [4, 5, 6]
  }
  @FeatureInfo(id = "6cca6feb-8049-4d12-82fe-5534e41e311b",impact = 3,frequencyOfFailure = 1,component = "Feature Unrolling")
  @Unroll
  def "with unrolling"() {
    expect:
    name.size() == length

    where:
    name << ["Kirk", "Spock", "Scotty"]
    length << [4, 5, 6]
  }
  @FeatureInfo(id = "01fdbb08-7163-4a8a-a806-aad04f34406b",impact = 2,frequencyOfFailure = 1,component = "Feature Unrolling")
  @Unroll("length of '#name' should be #length")
  def "with unrolling and custom naming pattern"() {
    expect:
    name.size() == length

    where:
    name << ["Kirk", "Spock", "Scotty"]
    length << [4, 5, 6]
  }
}
