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

package org.spockframework.runtime

import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.model.SpecInfo

import spock.lang.Specification

class ExtensionRegistrySpec extends Specification {
  def "registry can load extensions"() {
    def registry = new ExtensionRegistry([Extension1, Extension2], [])
    
    when:
    registry.loadExtensions()

    then:
    registry.extensions*.getClass() == [Extension1, Extension2]
  }
}

class Extension1 implements IGlobalExtension {
  void visitSpec(SpecInfo spec) {}
}

class Extension2 implements IGlobalExtension {
  void visitSpec(SpecInfo spec) {}
}