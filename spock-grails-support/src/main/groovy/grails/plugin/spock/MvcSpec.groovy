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

import org.codehaus.groovy.grails.commons.ApplicationAttributes
import org.codehaus.groovy.grails.support.MockApplicationContext
import org.codehaus.groovy.grails.web.pages.DefaultGroovyPagesUriService
import org.codehaus.groovy.grails.web.pages.GroovyPagesUriService
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Shared

/**
 * Support class for writing unit tests for controllers. Its main job
 * is to mock the various properties and methods that Grails injects
 * into controllers. By default it determines what controller to mock
 * based on the name of the test, but this can be overridden by one
 * of the constructors.
 * 
 * @author Graeme Rocher
 * @author Peter Ledbrook
 */
abstract class MvcSpec extends UnitSpec {
  @Shared classUnderTest
  def instanceUnderTest
  
  protected webRequest

  abstract provideMvcClassUnderTest() 
  abstract initializeMvcMocking(Class classUnderTest)
  
  def setupSpec() {
    classUnderTest = provideMvcClassUnderTest()
  }
  
  def findClassUnderTestConventiallyBySuffix(suffix) {
    def matcher = getClass().name =~ /^([\w\.]*?[A-Z]\w*?${suffix})\w+/
    if (matcher) {
      def classUnderTestName = matcher[0][1]
      classUnderTest = Thread.currentThread().contextClassLoader.loadClass(classUnderTestName)
    } else {
      throw new RuntimeException("Can not find classUnderTest conventionally for ${this.class.name} as it doesn't contain $suffix".toString())
    }
  }
  
  def getMockRequest() { instanceUnderTest.request }
  def getMockResponse() { instanceUnderTest.response }
  def getMockSession() { instanceUnderTest.session }

  def getRenderArgs() { instanceUnderTest.renderArgs }
  
  def getMockParams() { instanceUnderTest.params }
  def getMockFlash() { instanceUnderTest.flash }

  def setup() {
    initializeMvcMocking(classUnderTest)
    instanceUnderTest = classUnderTest.newInstance()

    MockApplicationContext ctx = new MockApplicationContext()
    ctx.registerMockBean(GroovyPagesUriService.BEAN_ID, new DefaultGroovyPagesUriService())
    mockRequest.servletContext.setAttribute(ApplicationAttributes.APPLICATION_CONTEXT, ctx)
    
    webRequest = new GrailsWebRequest(
        mockRequest,
        mockResponse,
        mockRequest.servletContext
   )
   
    mockRequest.setAttribute(GrailsApplicationAttributes.WEB_REQUEST, webRequest)
    RequestContextHolder.setRequestAttributes(webRequest)
  }

  void reset() {
    mockRequest.clearAttributes()
    mockRequest.removeAllParameters()
    mockResponse.committed = false
    mockSession.clearAttributes()
    mockSession.setNew(true)

    renderArgs.clear()

    mockParams.clear()
    mockFlash.clear()
  }
}
