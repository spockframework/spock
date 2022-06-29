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

package org.spockframework.tapestry

import org.apache.tapestry5.ioc.RegistryBuilder
import org.apache.tapestry5.ioc.annotations.*

import spock.lang.*

@SubModule(Module1)
class BeforeRegistryCreatedMethod extends Specification {
  @Shared
  @Inject
  IService1 sharedServiceField

  @Inject
  IService1 instanceServiceField

  @Shared
  String sharedField = "sharedField"

  String instanceField = "instanceField"

  @Shared
  boolean beforeRegistryCreatedRun

  def beforeRegistryCreated() {
    assert sharedServiceField == null
    assert instanceServiceField == null
    assert sharedField == null
    assert instanceField == null
    assert !beforeRegistryCreatedRun
    beforeRegistryCreatedRun = true
  }

  def beforeSpec() {
    assert sharedServiceField instanceof Service1
    assert instanceServiceField == null
    assert sharedField == "sharedField"
    assert instanceField == null
    assert beforeRegistryCreatedRun
  }

  def feature() {
    expect:
    sharedServiceField instanceof IService1
    instanceServiceField instanceof IService1
    sharedField == "sharedField"
    instanceField == "instanceField"
    beforeRegistryCreatedRun
  }
}

@SubModule([])
class CustomTapestryRegistry extends Specification {
  @Inject
  IService1 service1

  def beforeRegistryCreated() {
    def builder = new RegistryBuilder()
    builder.add(Module1)
    def registry = builder.build()
    registry.performRegistryStartup()
    registry
  }

  def "custom registry is used for injection"() {
    expect:
    service1 instanceof IService1
  }
}
