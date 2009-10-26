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

package grails.plugin.spock

import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine
import org.springframework.web.context.request.RequestContextHolder
import org.codehaus.groovy.grails.web.util.GrailsPrintWriter

class GroovyPagesSpecification extends IntegrationSpecification {
  GroovyPagesTemplateEngine groovyPagesTemplateEngine // autowired
  
  String template = ''
  Map params = [:]
  Closure transform = { it.toString() }
  
  void setControllerName(String name) {
    RequestContextHolder.currentRequestAttributes().controllerName = name
  }
  
  def getOutput() {
    assert groovyPagesTemplateEngine
    
    def webRequest = RequestContextHolder.currentRequestAttributes()
    
    def t = groovyPagesTemplateEngine.createTemplate(template, "test_"+ System.currentTimeMillis())
    def w = t.make(params)

    def sw = new StringWriter()
    def out = new GrailsPrintWriter(sw)
    webRequest.out = out
    w.writeTo(out)

    transform(sw)
  }
}