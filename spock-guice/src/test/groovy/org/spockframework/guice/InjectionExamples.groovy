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

package org.spockframework.guice

import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.name.Named
import spock.guice.UseModules
import spock.lang.Shared
import spock.lang.Specification

@UseModules(Module1)
class InjectionExamples extends Specification {
  @Inject
  IService1 service

  @Inject
  @Shared
  IService1 sharedService

  @Inject
  @Named("value1")
  String namedValue1

  @Inject
  @Named("value2")
  String namedValue2

  @Inject
  @BindingAnnotation1
  String annotatedValue1

  @Inject
  @BindingAnnotation2
  String annotatedValue2

  IService1 copiedService = service

  @Shared
  IService1 copiedSharedService = sharedService

  @Inject
  Injector injector

  @Shared
  List savedServices = []

  @Shared
  List savedSharedServices = []

  def setupSpec() {
    assert sharedService instanceof Service1
  }

  def cleanupSpec() {
    assert sharedService instanceof Service1
  }

  def setup() {
    assert service instanceof Service1
    assert sharedService instanceof Service1

    savedServices << service
    savedSharedServices << sharedService
  }

  def cleanup() {
    assert service instanceof Service1
    assert sharedService instanceof Service1
  }

  def "injecting a field"() {
    expect:
    service instanceof Service1
  }

  def "injecting a shared field"() {
    expect:
    sharedService instanceof Service1
  }

  def "using @Named"() {
    expect:
    namedValue1 == "named value 1"
    namedValue2 == "named value 2"
  }

  def "using a binding annotation"() {
    expect:
    annotatedValue1 == "annotated value 1"
    annotatedValue2 == "annotated value 2"
  }

  def "accessing injected values from field initializers"() {
    expect:
    copiedService == service
    copiedSharedService == sharedService
  }

  def "explicit use of injector (discouraged)"() {
    expect:
    injector.getInstance(IService1) instanceof Service1
  }

  def "instance fields are injected once per feature iteration"() {
    expect:
    (savedServices as Set).size() == savedServices.size()
  }

  def "shared fields are injected once per specification"() {
    expect:
    (savedSharedServices as Set).size() == 1
  }
}
