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

package org.spockframework.runtime.extension

import org.spockframework.runtime.model.SpecInfo
import spock.lang.Specification

class ExtensionRegistrySpec extends Specification {
  def "registry provides all available extensions"() {
    def loader = new ExtensionLoader(getClass().classLoader)
    def registry = new ExtensionRegistry(loader)
    
    expect:
    registry.extensions.size() == 2
    registry.extensions[0] instanceof Extension1
    registry.extensions[1] instanceof Extension2
  }
}

class Extension1 implements IGlobalExtension {
  void visitSpec(SpecInfo spec) {}
}

class Extension2 implements IGlobalExtension {
  void visitSpec(SpecInfo spec) {}
}

class ExtensionLoader extends ClassLoader {
  ExtensionLoader(parent) {
    super(parent);
  }

  protected Enumeration<URL> findResources(String name) {
    if (!name.equals(ExtensionRegistry.DESCRIPTOR_PATH))
      return super.findResources(name)

    URL url1 = getResource("org/spockframework/runtime/extension/descriptor1")
    URL url2 = getResource("org/spockframework/runtime/extension/descriptor2")
    assert url1 != null
    assert url2 != null
    
    return Collections.enumeration(Arrays.asList(url1, url2));
  }
}