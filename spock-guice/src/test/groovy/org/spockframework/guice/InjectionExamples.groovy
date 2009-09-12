/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.guice

import com.google.inject.Inject

import spock.guice.UseModules
import spock.lang.Shared
import spock.lang.Specification
import com.google.inject.Injector

@UseModules(Module)  
class InjectionExamples extends Specification {
  @Inject
  IService service

  @Inject
  @Shared
  IService sharedService

  IService copiedService = service

  @Shared
  IService copiedSharedService = sharedService

  @Inject
  Injector injector
  
  def setupSpeck() {
    assert sharedService instanceof Service
  }

  def cleanupSpeck() {
    assert sharedService instanceof Service
  }

  def setup() {
    assert service instanceof Service
    assert sharedService instanceof Service
  }

  def cleanup() {
    assert service instanceof Service
    assert sharedService instanceof Service
  }
  
  def "injecting a field"() {
    expect:
    service instanceof Service
  }

  def "injecting a shared field"() {
    expect:
    sharedService instanceof Service
  }

  def "injected values can be accessed from field initializers"() {
    expect:
    copiedService == service
    copiedSharedService == sharedService
  }

  def "explicit use of injector (discouraged)"() {
    expect:
    injector.getInstance(IService) instanceof Service
  }
}