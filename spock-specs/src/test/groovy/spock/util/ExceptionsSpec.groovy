/*
 * Copyright 2012 the original author or authors.
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
package spock.util

import spock.lang.Specification

class ExceptionsSpec extends Specification {
  def exception = new RuntimeException()
  def cause = new IllegalStateException()
  def cause2 = new FileNotFoundException()

  def setup() {
    exception.initCause(cause)
    cause.initCause(cause2)
  }

  def "get root cause"() {
    expect:
    Exceptions.getRootCause(exception) == cause2
    Exceptions.getRootCause(cause) == cause2
    Exceptions.getRootCause(cause2) == cause2
  }

  def "get cause chain"() {
    expect:
    Exceptions.getCauseChain(exception) == [exception, cause, cause2]
    Exceptions.getCauseChain(cause) == [cause, cause2]
    Exceptions.getCauseChain(cause2) == [cause2]
  }
}
