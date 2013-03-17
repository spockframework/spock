/*
 * Copyright 2013 the original author or authors.
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

package org.spockframework.report.sample

import spock.lang.*

@Narrative("""
In order to increase the ninja survival rate
As a ninja commander
I want my ninjas to decide whether to take on an opponent based on their skill levels
""")
@Title("Fight or Flight (Story Style)")
class FightOrFlightStory extends Specification {
  @Issue("260")
  def "Strong Opponent"() {
    given: "the ninja has a third level black-belt"
    System.out << getClass().getResourceAsStream("loremipsum.txt")

    when: "attacked by Chuck Norris"
    impl()

    then: "the ninja should run for his life"
    impl()
  }

  @Ignore
  def "Invisible Opponent"() {
    given: "the ninja has a third level black-belt"
    impl()

    when: "attacked by an invisible creature"
    impl()

    then: "the ninja should cast a 'tee-mac-woo' spell"
    impl()

    and: "hope for the best"
    impl()
  }

  @See(["http://en.wikipedia.org/wiki/Ninja", "http://en.wikipedia.org/wiki/Sega_Ninja"])
  def "Weak Opponent"() {
    given: "the ninja has a third level black-belt"
    println "some"
    println "output"

    when: "attacked by a samurai"
    System.err.println("some")
    System.err.println("error output")

    then: "the ninja should engage the opponent"
    Math.max (1, 2) == 4
  }

  private void impl() {}
}
