/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.tapestry

import org.apache.tapestry5.ioc.ObjectLocator
import org.apache.tapestry5.ioc.annotations.*

import spock.lang.*

@ImportModule(Module1)
class ServiceInjectionWithImportModule extends Specification {
  @Inject
  IService1 service1

  @Inject
  IService2 service2

  def "injected services"() {
    expect:
    service1.generateString() == service2.generateQuickBrownFox()
  }
}

@ImportModule(Module1)
class ServiceInjectionWithJavaxInjectWithImportModule extends Specification {
  @javax.inject.Inject
  org.spockframework.tapestry.IService1 service1

  @javax.inject.Inject
  org.spockframework.tapestry.IService2 service2

  def "injected services"() {
    expect:
    service1.generateString() == service2.generateQuickBrownFox()
  }
}

@ImportModule(Module1)
class ServiceInjectionWithServiceIdWithImportModule extends Specification {
  @InjectService("Service3")
  IService3 service3

  @Inject
  IService2 service2

  def "injected services"() {
    expect:
    service3.generateString() == service2.generateQuickBrownFox()
  }
}

@ImportModule(Module1)
class ObjectLocatorInjectionWithImportModule extends Specification {
  @Inject
  private ObjectLocator locator

  def "injected object locator"() {
    expect:
    locator.getService(IService1).generateString() == locator.getService(IService2).generateQuickBrownFox()
  }
}

@ImportModule(Module1)
class SymbolInjectionWithImportModule extends Specification {
  @Inject
  @Symbol("java.version")
  String javaVersion

  @Inject
  @Symbol("configKey") // Groovy doesn't yet support constants in annotations, so we have to use a literal
  String configValue

  @Inject
  @Value('${java.version} and ${configKey}')
  String computedValue

  def "injected system property"() {
    expect:
    javaVersion == System.getProperty("java.version")
  }

  def "injected application default"() {
    expect:
    configValue == "configValue"
  }

  def "injected computed value"() {
    expect:
    computedValue == "${System.getProperty("java.version")} and configValue"
  }
}

@ImportModule(Module2)
class AutobuildInjectionWithImportModule extends Specification {
  @Autobuild
  Service1 service1

  def "auto-built service"() {
    expect:
    service1 instanceof Service1
  }
}
