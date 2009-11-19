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

import spock.lang.Specification

class Publisher {
  def subscribers = []

  def send(event) {
    subscribers.each {
      try {
        it.receive(event)
      } catch (Exception e) {}
    }
  }
}

interface Subscriber {
  def receive(event)
}

class PublisherSpec extends Specification {
  def pub = new Publisher()
  def sub1 = Mock(Subscriber)
  def sub2 = Mock(Subscriber)

  def setup() {
    pub.subscribers << sub1 << sub2
  }

  def "delivers events to all subscribers"() {
    when:
    pub.send("event")

    then:
    1 * sub1.receive("event")
    1 * sub2.receive("event")
  }

  def "can cope with misbehaving subscribers"() {
    sub1.receive(_) >> { throw new Exception() }

    when:
    pub.send("event1")
    pub.send("event2")

    then:
    1 * sub2.receive("event1")
    1 * sub2.receive("event2")
  }
}
