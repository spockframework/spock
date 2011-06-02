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

import grails.plugin.spock.ControllerSpec

class TestControllerSpec extends ControllerSpec {
  def 'text action'() {
    when:
    controller.text()
    
    then:
    mockResponse.contentAsString == "text"
  }

  def 'some redirect action'() {
    when:
    controller.someRedirect()
    
    then:
    redirectArgs == [action: "someRedirect"]
  }
  
  def 'bodyElementText action'() {
    when:
    xmlRequestContent = requestContent
    
    and:
    controller.bodyElementText()
    
    then:
    mockResponse.contentAsString == value
    
    where:
    value << ['value', 'value']
    requestContent << ["<request><body>value</body></request>", { request { body('value') } }]
  }

  def 'model action'() {
    expect:
    controller.model() == [a: '1']
  }
}

class TestController {
  def text = {
    render "text"
  }

  def someRedirect = {
    redirect(action: "someRedirect")
  }
  
  def bodyElementText = {
    render request.'XML'.body.text()
  }

  def model = {
    [a: '1']
  }
}