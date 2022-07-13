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

package org.spockframework.smoke.extension

import org.spockframework.runtime.model.parallel.Resources
import spock.lang.*
import spock.util.environment.RestoreSystemProperties

@ResourceLock(Resources.SYSTEM_PROPERTIES)
class RestoreSystemPropertiesExtension extends Specification {
  def setupSpec() {
    System.setProperty("RestoreSystemPropertiesExtension.prop1", "original value")
    System.setProperty("RestoreSystemPropertiesExtension.prop2", "original value")
  }

  @RestoreSystemProperties
  def "restores system properties for methods annotated with @RestoreSystemProperties"() {
    setup:
    System.setProperty("RestoreSystemPropertiesExtension.prop1", "new value")
  }

  def "does not restore system properties for methods not annotated with @RestoreSystemProperties"() {
    setup:
    System.setProperty("RestoreSystemPropertiesExtension.prop2", "new value")
  }

  def cleanupSpec() {
    assert System.getProperty("RestoreSystemPropertiesExtension.prop1") == "original value"
    assert System.getProperty("RestoreSystemPropertiesExtension.prop2") == "new value"
  }
}

@RestoreSystemProperties
class UseRestoreSystemPropertiesOnSpecClass extends Specification {
  def setupSpec() {
    System.setProperty("RestoreSystemPropertiesExtension.prop1", "original value")
    System.setProperty("RestoreSystemPropertiesExtension.prop2", "original value")
  }

  def "restores system properties for all methods (1)"() {
    setup:
    System.setProperty("RestoreSystemPropertiesExtension.prop1", "new value")
  }

  def "restores system properties for all methods (2)"() {
    setup:
    System.setProperty("RestoreSystemPropertiesExtension.prop1", "new value")
  }

  def cleanupSpec() {
    assert System.getProperty("RestoreSystemPropertiesExtension.prop1") == "original value"
    assert System.getProperty("RestoreSystemPropertiesExtension.prop2") == "original value"
  }
}
