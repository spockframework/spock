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
 
package org.spockframework.tapestry

import org.apache.tapestry5.ioc.annotations.Inject
import org.apache.tapestry5.ioc.annotations.SubModule
import spock.lang.Specification

@SubModule(PerIterationModule)
class PerIterationScope extends Specification {
  @Inject
  IPerIterationService service

  def "iteration 1"() {
    expect:
    service.get() == null

    service.set(1)
  }

  def "iteration 2"() {
    expect:
    service.get() == null

    service.set(2)
  }

  def "iteration 3 to 5"() {
    expect:
    service.get() == null
    
    service.set(iteration)

    where:
    iteration << (3..5)
  }
}