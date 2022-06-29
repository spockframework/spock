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

package org.spockframework.guice

import com.google.inject.Inject
import spock.guice.UseModules
import spock.lang.Specification

@UseModules(Module1)
@UseModules(Module2)
class GuiceMultiAnnotations extends Specification {
  @Inject
  IService1 service1

  @Inject
  IService2 service2

  @Inject
  IService2 anotherService2

  def setup() {
    assert service1 instanceof IService1
    assert service2 instanceof IService2
    assert anotherService2 instanceof IService2
  }

  def "fields have been injected"() {
    expect:
    service1 instanceof IService1
    service2 instanceof IService2
    anotherService2 instanceof IService2
  }
}
