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

package org.spockframework.runtime


import org.spockframework.runtime.model.parallel.Resources

import spock.config.ConfigurationException
import spock.lang.ResourceLock
import spock.lang.Specification

@ResourceLock(Resources.SYSTEM_PROPERTIES)
class ConfigurationScriptLoaderSpec extends Specification {
  private static final String PROP_KEY = "spockConfigSystemProperty"
  private static final String VALID_CONFIG = "org/spockframework/runtime/ValidConfig.txt"
  private static final String INVALID_CONFIG = "org/spockframework/runtime/InvalidConfig.txt"

  def "load configuration script from system property that points to class path location"() {
    System.setProperty(PROP_KEY, VALID_CONFIG)
    def loader = new ConfigurationScriptLoader(PROP_KEY, "foo", "bar")

    expect:
    loader.loadAutoDetectedScript() != null

    cleanup:
    System.clearProperty(PROP_KEY)
  }

  def "load configuration script from system property that points to file system location"() {
    def location = getClass().classLoader.getResource(VALID_CONFIG).path
    System.setProperty(PROP_KEY, location)
    def loader = new ConfigurationScriptLoader(PROP_KEY, "foo", "bar")

    expect:
    loader.loadAutoDetectedScript() != null

    cleanup:
    System.clearProperty(PROP_KEY)
  }

  def "system property that points to unexisting location is rejected"() {
    System.setProperty(PROP_KEY, "does/not/exist")
    def loader = new ConfigurationScriptLoader(PROP_KEY, "foo", "bar")

    when:
    loader.loadAutoDetectedScript()

    then:
    thrown(ConfigurationException)

    cleanup:
    System.clearProperty(PROP_KEY)
  }

  def "configuration script that cannot be compiled is rejected"() {
    System.setProperty(PROP_KEY, INVALID_CONFIG)
    def loader = new ConfigurationScriptLoader(PROP_KEY, "foo", "bar")

    when:
    loader.loadAutoDetectedScript()

    then:
    thrown(ConfigurationException)

    cleanup:
    System.clearProperty(PROP_KEY)
  }

  def "load configuration script from class path location"() {
    def loader = new ConfigurationScriptLoader("does/not/exist", VALID_CONFIG, "bar")

    expect:
    loader.loadAutoDetectedScript() != null
  }

  def "load configuration script from file system location"() {
    def location = getClass().classLoader.getResource(VALID_CONFIG).path
    def loader = new ConfigurationScriptLoader("does/not/exist", "foo", location)

    expect:
    loader.loadAutoDetectedScript() != null
  }
}
