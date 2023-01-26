/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.util


import spock.lang.Specification

class VersionNumberSpec extends Specification {
  def "parsing"() {
    expect:
    VersionNumber.parse(versionString) == parsedVersion

    where:
    versionString        | parsedVersion
    "1"                  | new VersionNumber(1, 0, 0, false, null)
    "1.0"                | new VersionNumber(1, 0, 0, false, null)
    "1.0.0"              | new VersionNumber(1, 0, 0, false, null)

    "1.2"                | new VersionNumber(1, 2, 0, false, null)
    "1.2.3"              | new VersionNumber(1, 2, 3, false, null)

    "1-SNAPSHOT"         | new VersionNumber(1, 0, 0, true, null)
    "1.2-SNAPSHOT"       | new VersionNumber(1, 2, 0, true, null)
    "1.2.3-SNAPSHOT"     | new VersionNumber(1, 2, 3, true, null)

    "1-rc1-SNAPSHOT"     | new VersionNumber(1, 0, 0, true, "rc1")
    "1.2-rc1-SNAPSHOT"   | new VersionNumber(1, 2, 0, true, "rc1")
    "1.2.3-rc1-SNAPSHOT" | new VersionNumber(1, 2, 3, true, "rc1")

    "1.rc1-SNAPSHOT"     | new VersionNumber(1, 0, 0, true, ".rc1", 1)
    "1.2.rc1-SNAPSHOT"   | new VersionNumber(1, 2, 0, true, ".rc1", 2)
    "1.2.3.rc1-SNAPSHOT" | new VersionNumber(1, 2, 3, true, ".rc1", 3)

    "11.22.33.44"        | new VersionNumber(11, 22, 33, false, ".44", 3)
    "11.44"              | new VersionNumber(11, 44, 0, false, null)
    "11.fortyfour"       | new VersionNumber(11, 0, 0, false, ".fortyfour", 1)
  }

  def "unparseable version number is represented as UNKNOWN (0.0.0)"(String version) {
    expect:
    VersionNumber.parse(version) == VersionNumber.UNKNOWN

    where:
    version << [
      null,
      "",
      "foo",
      "1.",
      "1.2.3-"
    ]
  }

  def "accessors"() {
    when:
    def version = new VersionNumber(1, 2, 3, false, "foo")

    then:
    version.major == 1
    version.minor == 2
    version.micro == 3
    version.qualifier == "foo"
    !version.snapshot
  }

  def "string representation"() {
    expect:
    versionNumber.toString() == expected

    where:
    versionNumber                            | expected
    new VersionNumber(1, 0, 0, false, null)  | "1.0.0"
    new VersionNumber(1, 2, 3, false, "foo") | "1.2.3-foo"
  }

  def "original representation"() {
    expect:
    VersionNumber.parse(versionString).toOriginalString() == versionString

    where:
    versionString << [
      "1",
      "1.0",
      "1.0.0",
      "1.2",
      "1.2.3",
      "1-SNAPSHOT",
      "1.2-SNAPSHOT",
      "1.2.3-SNAPSHOT",
      "1-rc1-SNAPSHOT",
      "1.2-rc1-SNAPSHOT",
      "1.2.3-rc1-SNAPSHOT",
      "1.rc1-SNAPSHOT",
      "1.2.rc1-SNAPSHOT",
      "1.2.3.rc1-SNAPSHOT",
      "11.22.33.44",
      "11.44",
      "11.fortyfour"
    ]
  }

  def "original representation customized"() {
    expect:
    VersionNumber.parse(versionString).toOriginalString(includeQualifier, includeSnapshot) == expected

    where:
    versionString        | includeSnapshot | includeQualifier | expected
    "1"                  | true            | false            | "1"
    "1.2"                | true            | false            | "1.2"
    "1.2.3"              | true            | false            | "1.2.3"
    "1-SNAPSHOT"         | true            | false            | "1-SNAPSHOT"
    "1.2-SNAPSHOT"       | true            | false            | "1.2-SNAPSHOT"
    "1.2.3-SNAPSHOT"     | true            | false            | "1.2.3-SNAPSHOT"
    "1-rc1-SNAPSHOT"     | true            | false            | "1-SNAPSHOT"
    "1.2-rc1-SNAPSHOT"   | true            | false            | "1.2-SNAPSHOT"
    "1.2.3-rc1-SNAPSHOT" | true            | false            | "1.2.3-SNAPSHOT"
    "1"                  | false           | true             | "1"
    "1.2"                | false           | true             | "1.2"
    "1.2.3"              | false           | true             | "1.2.3"
    "1-SNAPSHOT"         | false           | true             | "1"
    "1.2-SNAPSHOT"       | false           | true             | "1.2"
    "1.2.3-SNAPSHOT"     | false           | true             | "1.2.3"
    "1-rc1-SNAPSHOT"     | false           | true             | "1-rc1"
    "1.2-rc1-SNAPSHOT"   | false           | true             | "1.2-rc1"
    "1.2.3-rc1-SNAPSHOT" | false           | true             | "1.2.3-rc1"
    "1"                  | false           | false            | "1"
    "1.2"                | false           | false            | "1.2"
    "1.2.3"              | false           | false            | "1.2.3"
    "1-SNAPSHOT"         | false           | false            | "1"
    "1.2-SNAPSHOT"       | false           | false            | "1.2"
    "1.2.3-SNAPSHOT"     | false           | false            | "1.2.3"
    "1-rc1-SNAPSHOT"     | false           | false            | "1"
    "1.2-rc1-SNAPSHOT"   | false           | false            | "1.2"
    "1.2.3-rc1-SNAPSHOT" | false           | false            | "1.2.3"


  }

  def "equality"() {
    expect:
    new VersionNumber(1, 1, 1, false, null) == new VersionNumber(1, 1, 1, false, null)
    new VersionNumber(1, 1, 1, false, "foo") == new VersionNumber(1, 1, 1, false, ".foo", 3)
    new VersionNumber(1, 0, 0, false, "foo") == new VersionNumber(1, 0, 0, false, ".foo", 1)
    new VersionNumber(2, 1, 1, false, null) != new VersionNumber(1, 1, 1, false, null)
    new VersionNumber(1, 2, 1, false, null) != new VersionNumber(1, 1, 1, false, null)
    new VersionNumber(1, 1, 2, false, null) != new VersionNumber(1, 1, 1, false, null)
    new VersionNumber(1, 1, 1, false, null) != new VersionNumber(1, 1, 1, true, null)
    new VersionNumber(1, 1, 1, false, "foo") != new VersionNumber(1, 1, 1, false, null)
  }

  def "comparison"() {
    expect:
    (new VersionNumber(1, 1, 1, false, null) <=> new VersionNumber(1, 1, 1, false, null)) == 0

    (new VersionNumber(2, 1, 1, false, null) <=> new VersionNumber(1, 1, 1, false, null)) > 0
    (new VersionNumber(1, 2, 1, false, null) <=> new VersionNumber(1, 1, 1, false, null)) > 0
    (new VersionNumber(1, 1, 2, false, null) <=> new VersionNumber(1, 1, 1, false, null)) > 0
    (new VersionNumber(1, 1, 1, false, "foo") <=> new VersionNumber(1, 1, 1, false, null)) > 0
    (new VersionNumber(1, 1, 1, false, null) <=> new VersionNumber(1, 1, 1, true, null)) > 0
    (new VersionNumber(1, 1, 1, false, "b") <=> new VersionNumber(1, 1, 1, false, "a")) > 0

    (new VersionNumber(1, 1, 1, false, null) <=> new VersionNumber(2, 1, 1, false, null)) < 0
    (new VersionNumber(1, 1, 1, false, null) <=> new VersionNumber(1, 2, 1, false, null)) < 0
    (new VersionNumber(1, 1, 1, false, null) <=> new VersionNumber(1, 1, 2, false, null)) < 0
    (new VersionNumber(1, 1, 1, false, null) <=> new VersionNumber(1, 1, 1, false, "foo")) < 0
    (new VersionNumber(1, 1, 1, false, "a") <=> new VersionNumber(1, 1, 1, false, "b")) < 0
  }
}
