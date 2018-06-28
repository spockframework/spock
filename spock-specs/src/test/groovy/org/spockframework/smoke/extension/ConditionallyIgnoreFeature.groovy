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

package org.spockframework.smoke.extension

import spock.lang.*

import java.util.concurrent.CopyOnWriteArrayList

class ConditionallyIgnoreFeature extends Specification {
  @Shared List log = new CopyOnWriteArrayList()

  @IgnoreIf({ 1 < 2 })
  def "should be ignored"() {
    log << 1
    expect: false
  }

  @IgnoreIf({ 1 > 2 })
  def "should be run"() {
    log << 2
    expect: true
  }

  @IgnoreIf({ [1, 2, 3] })
  def "should be ignored according to Groovy truth"() {
    log << 3
    expect: false
  }

  @IgnoreIf({ [] })
  def "should be run according to Groovy truth"() {
    log << 4
    expect: true
  }

  @IgnoreIf({ javaVersion < 1.5 })
  def "provides convenient access to Java version"() {
    log << 5
    expect: true
  }

  @IgnoreIf({ env."PATH" != env["PATH"] })
  def "provides convenient access to environment variables"() {
    log << 6
    expect: true
  }

  @IgnoreIf({ sys."os.name" != sys["os.name"] })
  def "provides convenient access to system properties"() {
    log << 7
    expect: true
  }

  def cleanupSpec() {
    assert log as Set == [2, 4, 5, 6, 7] as Set
  }
}
