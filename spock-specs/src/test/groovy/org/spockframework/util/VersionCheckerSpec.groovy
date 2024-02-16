/*
 * Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.spockframework.util


import org.spockframework.runtime.model.parallel.ResourceAccessMode
import org.spockframework.runtime.model.parallel.Resources
import spock.lang.ResourceLock
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

class VersionCheckerSpec extends Specification {

  private static final String WHO_IS_CHECKING = "test"

  private VersionChecker versionChecker = Spy()

  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ)
  void "not throw exception on compatible Groovy version"() {
    when:
    versionChecker.checkGroovyVersion(WHO_IS_CHECKING)

    then:
    noExceptionThrown()
  }

  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ)
  void "throw exception on incompatible Groovy version"() {
    given:
    versionChecker.isCompatibleGroovyVersion() >> false

    when:
    versionChecker.checkGroovyVersion(WHO_IS_CHECKING)

    then:
    thrown(IncompatibleGroovyVersionException)
  }

  @ResourceLock(Resources.SYSTEM_ERR)
  @RestoreSystemProperties
  void "suppress throwing exception on incompatible Groovy version if check is disabled"() {
    given:
    versionChecker.isCompatibleGroovyVersion() >> false

    and:
    System.setProperty(VersionChecker.DISABLE_GROOVY_VERSION_CHECK_PROPERTY_NAME, "true");

    when:
    versionChecker.checkGroovyVersion(WHO_IS_CHECKING)

    then:
    noExceptionThrown()
  }
}
