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

import grails.plugin.spock.IntegrationSpec

import spock.lang.*

class TestIntegrationSpec extends IntegrationSpec {
  
  @Shared anotherService
  
  def simpleService

  @Shared messageSource
  
  def setupSpec() {
    assert messageSource != null
    assert anotherService != null
  }
  
  def "injected simpleService is present"() {
    when:
    simpleService.name = input

    then:
    simpleService.name == input

    where:
    input = "bob"
  }

  def "can write to database"() {
    expect:
    new Person(name: 'fred').save() != null
    Person.findByName('fred') != null
  }
}