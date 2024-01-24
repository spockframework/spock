/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.util

import spock.lang.Specification

class TeePrintStreamSpec extends Specification {
  def delegate1 = Mock(PrintStream)
  def delegate2 = Mock(PrintStream)
  def printStream = new TeePrintStream(delegate1, delegate2)

  def "forwards stream management methods to all delegates"() {
    when:
    printStream."$method"()

    then:
    1 * delegate1."$method"()
    1 * delegate2."$method"()

    where:
    method << ["flush", "close", "checkError", "clearError", ]
  }

  def "forwards print methods to all delegates"() {
    when:
    printStream.print(arg)

    then:
    1 * delegate1.print(arg)
    1 * delegate2.print(arg)

    where:
    arg << [true, "c" as char, 42, 42l, 42f, 42d, "hi" as char[], "hi", new Object()]
  }

  def "forwards println methods to all delegates"() {
    when:
    printStream.println(arg)

    then:
    1 * delegate1.println(arg)
    1 * delegate2.println(arg)

    where:
    arg << ["true", "c" as char, 42, 42l, 42f, 42d, "hi" as char[], "hi", new Object()]
  }

  def "forwards printf methods to all delegates"() {
    when:
    printStream.printf("%s %d", "foo", 42)

    then:
    1 * delegate1.printf("%s %d", "foo", 42)
    1 * delegate2.printf("%s %d", "foo", 42)

    when:
    printStream.printf(Locale.US, "%s %d", "foo", 42)

    then:
    1 * delegate1.printf(Locale.US, "%s %d", "foo", 42)
    1 * delegate2.printf(Locale.US, "%s %d", "foo", 42)
  }

  def "forwards append methods to all delegates"() {
    when:
    printStream.append(arg)

    then:
    1 * delegate1.append(arg)
    1 * delegate2.append(arg)

    where:
    arg << ["c" as char, new StringBuilder(), "foo"]
  }

  def "forwards append method with offset to all delegates"() {
    when:
    printStream.append("foobar", 2, 4)

    then:
    1 * delegate1.append("foobar", 2, 4)
    1 * delegate2.append("foobar", 2, 4)
  }

  def "forwards write method to all delegates"() {
    when:
    printStream.write(arg)

    then:
    1 * delegate1.write(arg)
    1 * delegate2.write(arg)

    where:
    arg << [[1, 2, 3] as byte[]]
  }
}
