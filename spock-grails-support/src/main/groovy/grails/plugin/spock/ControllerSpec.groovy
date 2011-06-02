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

import grails.test.MockUtils
import grails.util.GrailsNameUtils
import groovy.xml.StreamingMarkupBuilder

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
class ControllerSpec extends MvcSpec {
  def setup() {
    webRequest.controllerName = GrailsNameUtils.getLogicalPropertyName(controllerClass.name, "Controller")
  }

  def provideMvcClassUnderTest() {
    findClassUnderTestConventiallyBySuffix('Controller')
  }

  def initializeMvcMocking(Class classUnderTest) {
    mockController(classUnderTest)
  }

  def getControllerClass() {
    classUnderTest
  }

  def getController() {
    instanceUnderTest
  }

  def getForwardArgs() { instanceUnderTest.forwardArgs }

  def getRedirectArgs() { instanceUnderTest.redirectArgs }

  def getChainArgs() { instanceUnderTest.chainArgs }

  void reset() {
    super.reset()
    redirectArgs.clear()
    forwardArgs.clear()
    chainArgs.clear()
  }

  protected mockCommandObject(Class clazz) {
    registerMetaClass(clazz)
    MockUtils.mockCommandObject(clazz, errorsMap)
  }

  protected void setXmlRequestContent(content) {
    setXmlRequestContent("UTF-8", content)
  }

  protected void setXmlRequestContent(String encoding, content) {
    mockRequest.contentType = "application/xml; charset=$encoding"

    if (content instanceof Closure) {
      def xml = new StreamingMarkupBuilder(encoding: encoding).bind(content)
      def out = new ByteArrayOutputStream()
      out << xml

      mockRequest.contentType = "application/xml; charset=$encoding"
      mockRequest.content = out.toByteArray()
    } else {
      mockRequest.content = content.getBytes(encoding)
    }
  }
}
