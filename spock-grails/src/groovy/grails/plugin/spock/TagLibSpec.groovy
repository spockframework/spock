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

class TagLibSpec extends MvcSpec {
  def provideMvcClassUnderTest() {
    findClassUnderTestConventiallyBySuffix('TagLib')
  }
  
  def initializeMvcMocking(Class classUnderTest) {
    mockTagLib(classUnderTest)
  }

  def getTagLib() {
    instanceUnderTest
  }

  Class getTagLibClass() {
    classUnderTest
  }
  
  void reset() {
    super.reset()
    tagLib.out.buffer.delete(0, tagLib.out.buffer.size())
  }
  
  
  def methodMissing(String name, args) {
    if (args.size() > 2) throw new IllegalArgumentException("tags take a maximum of 2 arguments")
    invokeTag(name, *args)
  }
  
  def invokeTag(String name, Map params = [:], Closure body = null) {
    def tag = tagLib."$name"
    def out = (tag.maximumNumberOfParameters == 1 ? tag(params) : tag(params, body)).toString()
    reset()
    out
  }
}
