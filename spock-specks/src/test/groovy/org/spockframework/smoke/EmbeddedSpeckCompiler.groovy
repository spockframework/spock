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

package org.spockframework.smoke

/**
 * Utility class for creating Specks from String source.
 * 
 * @author Peter Niederwieser
 */
class EmbeddedSpeckCompiler {

  def loader = new GroovyClassLoader()

  Class compile(String source) {
    loader.parseClass(source)
  }

  Class compileWithImports(String source) {
    // one-liner keeps line numbers intact
    compile "package apackage; import org.junit.runner.RunWith; import spock.lang.*; ${source.trim()}"
  }

  Class compileSpeckBody(String source) {
    // one-liner keeps line numbers intact
    compileWithImports "@Speck @RunWith(Sputnik) class ASpeck { ${source.trim()} }"
  }

  Class compileFeatureBody(String source) {
    // one-liner keeps line numbers intact
    compileSpeckBody "def 'a feature'() { ${source.trim()} }"
  }
}