
/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.smoke.mock

import spock.lang.*
import org.spockframework.mock.TooFewInvocationsError
import org.spockframework.mock.TooManyInvocationsError
import org.spockframework.mock.WrongInvocationOrderError

class ErrorReporting extends Specification {
  Collaborator collaborator = Mock()
  def swallower = new Swallower(collaborator: collaborator)

  @FailsWith(TooFewInvocationsError)
  def "error 'too few invocations' is reported even if object under test swallows exception"() {
    when:
    swallower.swallow()

    then:
    3 * collaborator.work()
  }

  @FailsWith(TooManyInvocationsError)
  def "error 'too many invocations' is reported even if object under test swallows exception"() {
    when:
    swallower.swallow()

    then:
    1 * collaborator.work()
  }

  @FailsWith(WrongInvocationOrderError)
  def "error 'wrong invocation order' is reported even if object under test swallows exception"() {
    when:
    swallower.swallow()

    then:
    1 * collaborator.work()

    then:
    1 * collaborator.welcome()
  }

  static class Swallower {
    Collaborator collaborator

    def swallow() {
      try {
        collaborator.welcome()
        collaborator.work()
        collaborator.work()
      } catch (Throwable swallowed) {}
    }
  }

  interface Collaborator {
    def welcome()
    def work()
  }
}


