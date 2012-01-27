/*
 * Copyright 2011 the original author or authors.
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

package org.spockframework.builder

import org.spockframework.runtime.SpockConfigurationBuilder
import spock.lang.Specification
import org.spockframework.runtime.ModelSourceFactory

class SpockPropertiesConfigurationSpec extends Specification {
  def sourceFactory = new ModelSourceFactory()
  def builder = new SpockConfigurationBuilder()

  def "configure simple slots"() {
    when:
    builder.fromSource(sourceFactory.fromProperties(["spock.person.name": "fred", "spock.person.age": 30]))
    def spock = new SpockConfiguration()
    builder.build([spock])

    then:
    spock.person.name == "fred"
    spock.person.age == 30
    spock.person.balances == []
  }

  def "configure list slot"() {
    when:
    builder.fromSource(sourceFactory.fromProperties(["spock.person.balances": "3, 5, 8.9"]))
    def spock = new SpockConfiguration()
    builder.build([spock])

    then:
    spock.person.name == null
    spock.person.age == 0
    spock.person.balances == [3.0, 5.0, 8.9]
  }

  private static class SpockConfiguration {
    Person person = new Person()
  }
  
  private static class Person {
    String name
    int age
    List<BigDecimal> balances = []
  }
}
