/*
 * Copyright 2012 the original author or authors.
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
package spock.util

import org.spockframework.runtime.model.parallel.*
import spock.lang.*
import spock.util.environment.*

@Isolated
class OperatingSystemSpec extends Specification {
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ)
  def "determines name based on os.name system property"() {
    expect:
    OperatingSystem.current.name == System.getProperty("os.name")
  }

  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ)
  def "determines version based on os.name system property"() {
    expect:
    OperatingSystem.current.version == System.getProperty("os.version")
  }

  @RestoreSystemProperties
  def "determines family based on os.name system property"() {
    System.setProperty("os.name", osName)

    expect:
    OperatingSystem.current.family == os

    where:
    osName                 | os
    "Windows 7"            | OperatingSystem.Family.WINDOWS
    "Ubuntu Linux"         | OperatingSystem.Family.LINUX
    "Mac OS 10.8.2"        | OperatingSystem.Family.MAC_OS
    "Some SunOS version"   | OperatingSystem.Family.SOLARIS
    "FreeBSD 9.0"          | OperatingSystem.Family.OTHER
  }

  @RestoreSystemProperties
  def "provides convenience methods to test for family"() {
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
