/*
 * Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.docs.interaction

import org.spockframework.runtime.model.parallel.Resources
import spock.lang.Issue
import spock.lang.ResourceLock
import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
@ResourceLock(
  value = Resources.META_CLASS_REGISTRY,
  reason = "Global Mocks"
)
class GlobalMockDocSpec extends Specification {

  @Issue("https://github.com/spockframework/spock/issues/785")
  def "Global Spy usage"() {
    // tag::global-spy[]
    given:
    def publisher = new Publisher()
    def anySubscriber = GroovySpy(global: true, RealSubscriber)
    publisher.subscribers.add(new RealSubscriber())
    publisher.subscribers.add(new RealSubscriber())

    when:
    publisher.send("message")

    then:
    2 * anySubscriber.receive("message")
    // end::global-spy[]
  }

  def "Global GroovyMock mocks also constructor"() {
    // tag::global-mock-constructor[]
    given:
    GroovyMock(global: true, RealSubscriber)
    when:
    def sub = new RealSubscriber()
    then: "The GroovyMock(global: true) will also mock the constructor"
    sub == null
    // end::global-mock-constructor[]
  }

  def "Global GroovyMock but with real constructor"() {
    // tag::global-mock-constructor-real[]
    given:
    GroovyMock(global: true, RealSubscriber) {
      //Allow that the real constructor is called
      new RealSubscriber(*_) >> { callRealMethod() }
    }
    when:
    def sub = new RealSubscriber()
    then:
    sub instanceof RealSubscriber
    // end::global-mock-constructor-real[]
  }

  static class RealSubscriber implements Subscriber {
    @Override
    void receive(String message) {
    }
  }
}
