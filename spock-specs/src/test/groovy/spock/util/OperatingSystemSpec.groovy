/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spock.util

import spock.lang.Specification
import spock.util.environment.OperatingSystem
import spock.util.environment.RestoreSystemProperties;

@RestoreSystemProperties
class OperatingSystemSpec extends Specification {
  def "recognizes current operating system based on os.name system property"() {
    System.setProperty("os.name", osName)

    expect:
    OperatingSystem.current == os

    where:
    osName                 | os
    "Windows 7"            | OperatingSystem.WINDOWS
    "Ubuntu Linux"         | OperatingSystem.LINUX
    "Mac OS 10.8.2"        | OperatingSystem.MAC_OS
    "Some SunOS version"   | OperatingSystem.SOLARIS
    "FreeBSD 9.0"          | OperatingSystem.OTHER
  }

  def "provides convenience methods to test for current operating system"() {
    System.setProperty("os.name", osName)
    def os = OperatingSystem.current

    expect:
    holds.call(os)
    !notHolds.call(os)

    where:
    osName                 | holds          | notHolds
    "Windows 7"            | { it.windows } | { it.linux }
    "Ubuntu Linux"         | { it.linux }   | { it.macOs }
    "Mac OS 10.8.2"        | { it.macOs }   | { it.solaris }
    "Some SunOS version"   | { it.solaris } | { it.other }
    "FreeBSD 9.0"          | { it.other }   | { it.windows }
  }
}
