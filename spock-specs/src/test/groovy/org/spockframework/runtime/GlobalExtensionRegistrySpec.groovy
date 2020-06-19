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

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.extension.AbstractGlobalExtension
import org.spockframework.runtime.extension.ExtensionException
import org.spockframework.runtime.extension.builtin.IgnoreExtension
import org.spockframework.runtime.extension.builtin.IncludeExcludeExtension
import org.spockframework.util.InternalSpockError

import spock.config.ConfigurationObject
import spock.config.RunnerConfiguration
import spock.lang.Ignore
import spock.lang.Specification

class GlobalExtensionRegistrySpec extends Specification {
  def "only accepts configuration objects annotated with @ConfigurationObject"() {
    when:
    new GlobalExtensionRegistry([], [new RunnerConfiguration()])

    then:
    noExceptionThrown()

    when:
    new GlobalExtensionRegistry([], [new Object() {}])

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

  def "provides access to initial configuration objects"() {
    def config = new RunnerConfiguration()
    def settings = new MySettings()

    when:
    def registry = new GlobalExtensionRegistry([], [config, settings])

    then:
    registry.getConfigurationByName("runner").is(config)
    registry.getConfigurationByType(RunnerConfiguration).is(config)

    registry.getConfigurationByName("settings").is(settings)
    registry.getConfigurationByType(MySettings).is(settings)
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
    def config = new RunnerConfiguration()
    def registry = new GlobalExtensionRegistry([InjectableExtension], [config])

    when:
    registry.initializeGlobalExtensions()

    then:
    with(registry.globalExtensions[0], InjectableExtension) {
      config.is(config)
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
    def runnerConfig = new RunnerConfiguration()
    def registry = new GlobalExtensionRegistry([], [runnerConfig])
    registry.initializeGlobalExtensions()

    when:
    def extension = new LocalExtension()
    registry.configureExtension(extension)

    then:
    extension.config.is(runnerConfig)
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

  // See https://github.com/spockframework/spock/issues/817
  def "Extensions discovered later in the classpath are ignored if already processed earlier"() {
    when:
    def registry = new GlobalExtensionRegistry(extensionClasses, [])
    registry.initializeGlobalExtensions()

    then:
    registry.globalExtensions*.class == expectedExtensionClasses

    where:
    // Ignore subsequent additions, even if they appear later in the list
    extensionClasses                                                 || expectedExtensionClasses
    [MyExtension, MyExtension]                                       || [MyExtension]
    [SpringExtension, MyExtension, SpringExtension]                  || [SpringExtension, MyExtension]
    [SpringExtension, SpringExtension, MyExtension, SpringExtension] || [SpringExtension, MyExtension]
  }

  static class SpringExtension extends AbstractGlobalExtension {

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
