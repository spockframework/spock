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

import grails.plugin.spock.TagLibSpecification

class TestTagLibSpecification extends TagLibSpecification {
  def 'bar tag'() {
    expect: 
    bar() == "<p>Hello World!</p>"
  }
  
  def 'bar tag subsequent call'() {
    expect: 
    bar() == "<p>Hello World!</p>"
    bar() == "<p>Hello World!</p>"
  }
  
  def 'body tag'() {
    expect: 
    bodyTag(name: 'a') { "Foo" } == "<a>Foo</a>"
    bodyTag(name: 'b') { "Bar" } == "<b>Bar</b>"
  }
}

class TestTagLib {
  def bar = { attrs, body ->
    out << "<p>Hello World!</p>"
  }

  def bodyTag = { attrs, body ->
    out << "<${attrs.name}>"
    out << body()
    out << "</${attrs.name}>"
  }
}