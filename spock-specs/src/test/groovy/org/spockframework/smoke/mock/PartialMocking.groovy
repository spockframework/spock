/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.smoke.mock

import spock.lang.Specification
import spock.lang.FailsWith
import org.spockframework.mock.TooFewInvocationsError

class PartialMocking extends Specification {
  def "can mock method on same object"() {
    def persister = Spy(MessagePersister)

    when:
    persister.receive("msg")

    then:
    1 * persister.store("msg")
  }

  def "can stub method on same object"() {
    def persister = Spy(MessagePersister) {
      isStorable(_) >> false
    }

    when:
    persister.receive("msg")

    then:
    0 * persister.store("msg")
  }

  @FailsWith(TooFewInvocationsError)
  def "fails if interaction not satisfied"() {
    def persister = Spy(MessagePersister)

    when:
    persister.receive("msg")

    then:
    2 * persister.store("msg")
  }

  def "cannot stub (or mock) private method"() {
    def persister = Spy(PrivateMessagePersister) {
      isStorable(_) >> false
    }

    when:
    persister.receive("msg")

    then:
    1 * persister.store("msg")
  }

  static class MessagePersister {
    void receive(String msg) {
      if (isStorable(msg)) store(msg)
    }

    boolean isStorable(msg) { true }

    void store(String msg) {}
  }

  static class PrivateMessagePersister {
    void receive(String msg) {
      if (isStorable(msg)) store(msg)
    }

    private boolean isStorable(msg) { true }

    void store(String msg) {}
  }
}
