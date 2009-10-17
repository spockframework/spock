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

package spock.util

import org.spockframework.runtime.SpeckInfoBuilder
import org.junit.runner.RunWith
import org.junit.Test

/**
 * Utility class for creating Specks from String source. <em>Not</em> thread-safe.
 * 
 * @author Peter Niederwieser
 */
class EmbeddedSpeckCompiler {
  final GroovyClassLoader loader = new GroovyClassLoader()

  /**
   * Compiles the given source code, and returns all Spock specifications
   * contained therein (but not other classes).
   */
  List compile(String source) {
    loader.clearCache()
    loader.parseClass(source.trim())
    loader.loadedClasses.findAll {
      SpeckInfoBuilder.isSpecification(it) || isJUnitTest(it) // need JUnit tests sometimes
    } as List
  }

  List compileWithImports(String source) {
    // one-liner keeps line numbers intact
    compile "package apackage; import org.junit.runner.RunWith; import spock.lang.*; ${source.trim()}"
  }

  Class compileSpeckBody(String source) {
    // one-liner keeps line numbers intact
    compileWithImports("@Speck @RunWith(Sputnik) class ASpeck { ${source.trim()} }")[0]
  }

  Class compileFeatureBody(String source) {
    // one-liner keeps line numbers intact
    compileSpeckBody "def 'a feature'() { ${source.trim()} }"
  }

  private boolean isJUnitTest(Class clazz) {
    clazz.isAnnotationPresent(RunWith) || clazz.methods.any { it.getAnnotation(Test) }
  }
}