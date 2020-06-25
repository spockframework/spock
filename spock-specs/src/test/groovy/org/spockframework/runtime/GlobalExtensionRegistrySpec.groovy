/*
 * Copyright 2013 the original author or authors.
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

package org.spockframework.runtime

import org.spockframework.runtime.extension.*
import org.spockframework.runtime.extension.builtin.*
import org.spockframework.util.InternalSpockError
import spock.config.*
import spock.lang.*

class GlobalExtensionRegistrySpec extends Specification {
  def "only accepts configuration objects annotated with @ConfigurationObject"() {
    when:
    new GlobalExtensionRegistry([], [RunnerConfiguration])

    then:
    noExceptionThrown()

    when:
    new GlobalExtensionRegistry([], [Object])

    then:
    thrown(InternalSpockError)
  }

  def "only accepts extension classes extending IGlobalExtension"() {
    when:
    new GlobalExtensionRegistry([IncludeExcludeExtension], []).initializeGlobalExtensions()

    then:
    noExceptionThrown()

    when:
    new GlobalExtensionRegistry([IgnoreExtension], []).initializeGlobalExtensions()

    then:
    thrown(ExtensionException)
  }

  def "instantiates extensions using their no-arg constructor"() {
    when:
    new GlobalExtensionRegistry([MyExtension], []).initializeGlobalExtensions()

    then:
    MyExtension.instantiated

    when:
    new GlobalExtensionRegistry([MissingNoArgCtorExtension], []).initializeGlobalExtensions()

    then:
    thrown(ExtensionException)
  }

  def "provides access to extensions"() {
    when:
    def registry = new GlobalExtensionRegistry([MyExtension, InjectableExtension], [])
    registry.initializeGlobalExtensions()

    then:
    registry.globalExtensions*.getClass() == [MyExtension, InjectableExtension]
  }

  def "auto-instantiates and provides access to global configuration objects"() {
    when:
    def registry = new GlobalExtensionRegistry([], [RunnerConfiguration, MySettings])

    then:
    registry.getConfigurationByName("runner") instanceof RunnerConfiguration
    registry.getConfigurationByType(RunnerConfiguration) instanceof RunnerConfiguration

    registry.getConfigurationByName("settings") instanceof MySettings
    registry.getConfigurationByType(MySettings) instanceof MySettings
  }

  def "auto-instantiates and provides access to configuration objects referenced by extensions"() {
    when:
    def registry = new GlobalExtensionRegistry([InjectableExtension], [])
    registry.initializeGlobalExtensions()

    then:
    registry.getConfigurationByName("runner") instanceof RunnerConfiguration
    registry.getConfigurationByType(RunnerConfiguration) instanceof RunnerConfiguration

    registry.getConfigurationByName("settings") instanceof MySettings
    registry.getConfigurationByType(MySettings) instanceof MySettings
  }

  def "injects configuration objects into extensions"() {
    def registry = new GlobalExtensionRegistry([InjectableExtension], [RunnerConfiguration])

    when:
    registry.initializeGlobalExtensions()

    then:
    with(registry.globalExtensions[0], InjectableExtension) {
      config instanceof RunnerConfiguration
      settings instanceof MySettings
    }
  }

  def "maintains a single instance of each configuration object type"() {
    def registry = new GlobalExtensionRegistry([InjectableExtension, SettingsExtension], [])

    when:
    registry.initializeGlobalExtensions()

    then:
    registry.getConfigurationByType(MySettings).is(registry.globalExtensions[0].settings)
    registry.globalExtensions[0].settings.is(registry.globalExtensions[1].settings)
  }

  def "allows to configure local extensions"() {
    def registry = new GlobalExtensionRegistry([], [RunnerConfiguration])
    registry.initializeGlobalExtensions()

    when:
    def extension = new LocalExtension()
    registry.configureExtension(extension)

    then:
    extension.config instanceof RunnerConfiguration
  }

  def "complains if local extension references unknown configuration class"() {
    def registry = new GlobalExtensionRegistry([], [])
    registry.initializeGlobalExtensions()

    when:
    def extension = new LocalExtension()
    registry.configureExtension(extension)

    then:
    ExtensionException e = thrown()
    e.message.contains("unknown configuration class")
  }

  static class MyExtension extends AbstractGlobalExtension {
    static instantiated = false

    MyExtension() {
      instantiated = true
    }
  }

  static class MissingNoArgCtorExtension extends AbstractGlobalExtension {
    MissingNoArgCtorExtension(int x) {}
  }

  static class InjectableExtension extends AbstractGlobalExtension {
    RunnerConfiguration config
    MySettings settings
  }

  static class SettingsExtension extends AbstractGlobalExtension {
    MySettings settings
  }

  static class LocalExtension extends AbstractAnnotationDrivenExtension<Ignore> {
    RunnerConfiguration config
  }

  @ConfigurationObject("settings")
  static class MySettings {
    String name
    int age
  }
}
