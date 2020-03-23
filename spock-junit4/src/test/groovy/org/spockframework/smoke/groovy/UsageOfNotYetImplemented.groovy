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
package org.spockframework.smoke.groovy

import groovy.transform.NotYetImplemented
import junit.framework.AssertionFailedError
import spock.lang.Specification
import spock.lang.FailsWith

//TODO: Handle also new groovy.transform.NotYetImplemented in Groovy 3 - https://github.com/spockframework/spock/issues/1127
class UsageOfNotYetImplemented extends Specification {

  @FailsWith(AssertionFailedError)
  @NotYetImplemented
  def "not allowed to pass"() {
    expect: true
  }

  @NotYetImplemented
  def "expected to fail (legacy transformation)"() {
    expect: false
  }

  @NotYetImplemented
  def "allowed to raise arbitrary exception (legacy transformation)"() {
    setup:
    throw new IOException("ouch")
  }

  @FailsWith(AssertionFailedError)
  @NotYetImplemented
  def "not allowed to pass (legacy transformation)"() {
    expect: true
  }
}
