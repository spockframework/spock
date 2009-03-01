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

package org.spockframework.smoke

import org.junit.runner.RunWith
import spock.lang.*
import org.spockframework.runtime.ConditionNotSatisfiedError

/**
 * A ...
 
 * @author Peter Niederwieser
 */
@Speck
@RunWith(Sputnik)
class MethodExecutionOrder {
  @Shared order = []

  def cleanup() {
    order << "a"
  }

  def cleanupSpeck() {
    assert order == ["bs","b",3,"a","b",4,"a","b",5,"a","b",6,"a","b",7,"a","b",8,"a","b",9,"a","b",10,"a"]
  }

  def "help, I need somebody"() {
    order << "h"
  }

  def "first feature"() {
    order << 3
    expect: true
  }

  def secondFeatureWithStandardName() {
    order << 4
    expect: true
  }

  def "just helping out"() {
    order << "h"
  }

  def "strangely named feature"() {
    order << 5
    expect: 1
  }

  def "very very long name but still nothing but a feature"() {
    order << 6
    expect: 1
  }

  def "short one"() {
    order << 7
    def x = "blah"
    when: x = x.reverse()
    then: x == "halb"
  }

  def setup() {
    order << "b"
  }

  def "helpless"() {
    order << "h"
  }

  def setupSpeck() {
    order << "bs"
  }

  def "parameterized feature"() {
    order << x
    expect: 1
    where : x << [8, 9, 10]
  }
}