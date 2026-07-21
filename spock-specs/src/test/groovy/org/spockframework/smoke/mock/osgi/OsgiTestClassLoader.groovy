/*
 * Copyright 2026 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.spockframework.smoke.mock.osgi

import groovy.transform.CompileStatic

import static java.util.Objects.requireNonNull

/**
 * Fakes an OSGi like ClassLoader env, by not using the parent ClassLoader semantics.
 */
@CompileStatic
class OsgiTestClassLoader extends ClassLoader {
  private final ClassLoader hostCl

  OsgiTestClassLoader(ClassLoader hostCl) {
    super(null)
    this.hostCl = requireNonNull(hostCl)
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    if (!name.startsWith("org.spockframework.smoke.mock.osgi.testclasses")) {
      return hostCl.loadClass(name)
    }
    super.loadClass(name, resolve)
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    def clsFilePath = name
    clsFilePath = clsFilePath.replace(".", "/")
    clsFilePath += ".class"
    def is = hostCl.getResourceAsStream(clsFilePath)
    if (is == null) {
      throw new ClassNotFoundException(name)
    }
    def clsData = is.getBytes()
    return defineClass(name, clsData, 0, clsData.length)
  }

  @Override
  URL getResource(String name) {
    return hostCl.getResource(name)
  }

  @Override
  Enumeration<URL> getResources(String name) throws IOException {
    return hostCl.getResources(name)
  }
}
