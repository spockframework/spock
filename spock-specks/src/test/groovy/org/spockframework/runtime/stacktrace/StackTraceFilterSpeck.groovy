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

package org.spockframework.runtime.stacktrace

import spock.lang.*
import static spock.lang.Predef.*
import org.junit.runner.RunWith

import org.spockframework.runtime.StackTraceFilter
import org.spockframework.runtime.IMethodNameMapper

/**
 * Lives in its own package because org.spockframework.runtime is filtered by
 * StackTraceFilter.
 * 
 * @author Peter Niederwieser
 */
@Speck(StackTraceFilter)
@RunWith(Sputnik)
class StackTraceFilterSpeck {
  def "filter trace"() {
    setup:
      def mapper = Mock(IMethodNameMapper)
      mapper.map("__feature0") >> "filter trace"
      def filter = new StackTraceFilter(mapper)

      Throwable t
      try {
        callChain.a()
        assert false
      } catch (CallChainException e) {
        t = e
      }

    when:
      filter.filter(t)

    then:
      def trace = t.stackTrace
      trace.size() >= 4

      def idx = trace.findIndexOf { it.methodName == "c" }
      idx != -1
      trace[++idx].methodName == "b"
      trace[++idx].methodName == "a"
      trace[++idx].methodName == "filter trace"

    where:
      callChain << [new GroovyCallChain(), new JavaCallChain()]
  }
}