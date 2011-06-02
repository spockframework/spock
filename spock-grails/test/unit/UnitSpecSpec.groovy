/* Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import grails.plugin.spock.UnitSpec
import org.codehaus.groovy.grails.test.io.SystemOutAndErrSwapper

import spock.lang.*

class UnitSpecSpec extends UnitSpec {
  def "domain mocking"() {
    setup:
      mockDomain(Person)
      
    when: "a person is saved"
      new Person(name: name).save()
    
    then: "it can be retrieved using a dynamic finder installed by mockDomain"
      Person.findByName(name) != null
      
    where:
      name = "bill"
  }
  
  def "constraints mocking"() {
    setup:
      mockForConstraintsTests(Person)
    
    when: 
      def p = new Person(name: name)
      
    then:
      p.validate() == validationResult
    
    where:
      name << [null, "something"]
      validationResult << [false, true]
  }

  @Ignore("Something weird about this with 1.4, UnitSpecSpecLoggingSubject has a log even without the mockLogging()")
  def "log mocking"() {
    setup:
      mockLogging(UnitSpecSpecLoggingSubject)
      def s = new UnitSpecSpecLoggingSubject()
      def out
      
    when:
      ioSwapper.swapIn()
      s.logIt(it)
    
    then:
      ioSwapper.swapOut().first().toString() == "INFO (${s.class.name}): log statement\n"
    
    where:
      ioSwapper = new SystemOutAndErrSwapper()
      it = "log statement"
  }
  
}

class UnitSpecSpecLoggingSubject {
  void logIt(it) {
    log.info(it)
  }
}