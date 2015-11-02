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

@Issue("260")
@Title("Fight or Flight (Spec Style)")
class FightOrFlightSpec extends Specification {
  def "Ninja should flee when encountering a strong opponent"() {
    given:
    System.out << getClass().getResourceAsStream("loremipsum.txt")

    when:
    impl()

    then:
    impl()
  }

  @Ignore
  def "Ninja should cast a tee-mac-woo spell when attacked by an invisible opponent (and hope for the best)"() {
    given:
    impl()

    when:
    impl()

    then:
    impl()

    and:
    impl()
  }

  @Ignore
  def "Ninja should cast a tee-mac-woo spell when attacked by an invisible opponent (but hope for the best)"() {
    given:
    impl()

    when:
    impl()

    then:
    impl()

    but:
    impl()
  }

  @See(["http://en.wikipedia.org/wiki/Ninja", "http://en.wikipedia.org/wiki/Sega_Ninja"])
  def "Ninja should engage a weak opponent"() {
    given:
    println "some"
    println "output"

    when:
    System.err.println("some")
    System.err.println("error output")

    then:
    Math.max (1, 2) == 2
  }

  private void impl() {}
}
