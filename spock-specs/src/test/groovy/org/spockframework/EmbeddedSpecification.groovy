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

package org.spockframework

import spock.lang.Specification
import spock.util.*
/**
 * Convenience base class for specifications that need to compile
 * and/or run other specifications.
 *
 * @author Peter Niederwieser
 */
abstract class EmbeddedSpecification extends Specification {
  EmbeddedSpecRunner runner = new EmbeddedSpecRunner()
  EmbeddedSpecCompiler compiler = new EmbeddedSpecCompiler()

  def javaVersion = System.getProperty("java.specification.version") as BigDecimal

  void stackTraceLooksLike(Throwable exception, String template) {

    List<StackTraceElement> trace = filterStackTraceWithJavaSpecificCalls(exception)

    def lines = template.trim().split("\n")
    assert trace.size() == lines.size()

    lines.eachWithIndex { line, index ->
      def traceElem = trace[index]
      def parts = line.split("\\|")
      def className = parts[0].trim()
      def methodName = parts[1].trim()
      def lineNumber = parts[2].trim()
      assert className == "-" || className == traceElem.className
      assert methodName == "-" || methodName == traceElem.methodName
      assert lineNumber == "-" || lineNumber as int == traceElem.lineNumber
    }
  }

  private List<StackTraceElement> filterStackTraceWithJavaSpecificCalls(Throwable exception) {
    List<StackTraceElement> stacktraceWithoutIndyCalls = exception.stackTrace
      .findAll { it["fileName"] != "IndyInterface.java" || it['methodName'] != "fromCache" }

    if (javaVersion >= 9) {
      return stacktraceWithoutIndyCalls.findAll { it['moduleName'] != "java.base" }
    } else {
      return stacktraceWithoutIndyCalls
    }
  }

}
