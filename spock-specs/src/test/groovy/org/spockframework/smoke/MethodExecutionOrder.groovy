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

package org.spockframework.smoke

import spock.lang.*

/**
 * @author Peter Niederwieser
 */
@Stepwise
class MethodExecutionOrder extends Specification {
  @Shared order = []

  def cleanup() {
    getOrder() << "c"
  }

  def cleanupSpec() {
    assert getOrder() == ["ss","s",3,"c","s",4,"c","s",5,"c","s",6,"c","s",7,"c","s",8,"c","s",9,"c","s",10,"c"]
  }

  def "help, I need somebody"() {
    order << "h"
  }

  def "first feature"() {
    order << 3
    expect: true
  }

  def secondFeatureWithStandardName() {
    getOrder() << 4
    expect: true
  }

  def "just helping out"() {
    getOrder() << "h"
  }

  def "strangely named feature"() {
    getOrder() << 5
    expect: 1
  }

  def "very very long name but still nothing but a feature"() {
    getOrder() << 6
    expect: 1
  }

  def "short one"() {
    getOrder() << 7
    def x = "blah"
    when: x = x.reverse()
    then: x == "halb"
  }

  def setup() {
    getOrder() << "s"
  }

  def "helpless"() {
    getOrder() << "h"
  }

  def setupSpec() {
    getOrder() << "ss"
  }

  def "parameterized feature"() {
    getOrder() << x
    expect: 1
    where: x << [8, 9, 10]
  }
}
