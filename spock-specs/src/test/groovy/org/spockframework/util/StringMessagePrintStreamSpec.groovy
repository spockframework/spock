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

class StringMessagePrintStreamSpec extends Specification {
  def log

  def notifier = new StringMessagePrintStream() {
    @Override
    protected void printed(String message) {
      log = message
    }
  }

  def "notifies write methods"() {
    when:
    notifier.write(input)

    then:
    log == output

    where:
    input                  | output
    65                     | "A"
    [65, 66, 67] as byte[] | "ABC"
  }

  def "notifies write method w/ offset"() {
    when:
    notifier.write([65, 66, 67, 68, 69] as byte[], 1, 3)

    then:
    log == "BCD"
  }

  def "notifies print methods"() {
    when:
    notifier.print(input)

    then:
    log == output

    where:
    input          | output
    true           | "true"
    "c" as char    | "c"
    42             | "42"
    42l            | "42"
    42f            | "42.0"
    42d            | "42.0"
    "hi" as char[] | "hi"
    "hi"           | "hi"
  }

  def "notifies println methods"() {
    when:
    notifier.println(input)

    then:
    log == output

    where:
    input          | output
    true           | "true\n"
    "c" as char    | "c\n"
    42             | "42\n"
    42l            | "42\n"
    42f            | "42.0\n"
    42d            | "42.0\n"
    "hi" as char[] | "hi\n"
    "hi"           | "hi\n"
  }

  def "notifies append methods"() {
    when:
    notifier.append(input)

    then:
    log == output

    where:
    input                           | output
    "foo"                           | "foo"
    new StringBuilder()
        .with { append("bar"); it } | "bar"
    "c" as char                     | "c"
  }

  def log2 = []

  def notifier2 = new StringMessagePrintStream() {
    @Override
    protected void printed(String message) {
      log2 << message
    }
  }

  def "notifies format method"() {
    when:
    notifier2.format("%s %d", "foo", 42)

    then:
    log2.join("") == "foo 42"
  }

  def "notifies format method w/ locale"() {
    when:
    notifier2.format(Locale.US, "%s %d", "foo", 42)

    then:
    log2.join("") == "foo 42"
  }

  def "notifies printf method"() {
    when:
    notifier2.printf("%s %d", "foo", 42)

    then:
    log2.join("") == "foo 42"
  }

  def "notifies printf method w/ locale"() {
    when:
    notifier2.printf(Locale.US, "%s %d", "foo", 42)

    then:
    log2.join("") == "foo 42"
  }
}
