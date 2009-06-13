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

import org.junit.runner.Result
import org.junit.runner.JUnitCore

/**
 * Utility methods for running embedded Specks.
 *
 * @author Peter Niederwieser
 */
class EmbeddedSpeckRunner {
  static run(String source) {
    def clazz = new GroovyClassLoader().parseClass(source)
    Result result = JUnitCore.runClasses(clazz)
    if (result.failureCount >= 1) throw result.failures[0].exception
  }

  static runWithHeader(String source) {
    // one-liner keeps line numbers intact
    run "package apackage; import org.junit.runner.RunWith; import spock.lang.*; ${source.trim()}"
  }

  static runSpeckBody(String body) {
    // one-liner keeps line numbers intact
    runWithHeader "@Speck @RunWith(Sputnik) class ASpeck { ${body.trim()} }"
  }

  static runFeatureBody(String body) {
    // one-liner keeps line numbers intact
    runSpeckBody "def 'a feature'() { ${body.trim()} }"
  }
}