/*
 * Copyright 2025 the original author or authors.
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
package org.spockframework

import org.junit.platform.commons.support.ReflectionSupport
import org.junit.platform.engine.TestEngine
import org.junit.platform.launcher.Launcher
import spock.lang.Specification

/**
 * The specification shall verify that Spock can run with JUnit 6 as a runtime platform.
 */
class JUnit6IntegrationSpec extends Specification {

  def "Simple JUnit 6 test"() {
    expect:
    true
  }

  def "Verify JUnit version of JUnit classes"() {
    expect:
    cls.package.implementationVersion.startsWith("6.")

    where:
    cls << [
      ReflectionSupport.class,//junit-platform-commons
      Launcher.class,         //junit-platform-launcher
      TestEngine.class        //junit-platform-engine
    ]
  }
}
