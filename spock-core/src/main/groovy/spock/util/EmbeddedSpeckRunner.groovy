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

import org.junit.runner.Result
import org.junit.runner.JUnitCore
import org.junit.runner.Request
import org.junit.runner.notification.RunListener

/**
 * Utility class for running Specks from String source.
 *
 * @author Peter Niederwieser
 */
class EmbeddedSpeckRunner {
  private final EmbeddedSpeckCompiler compiler = new EmbeddedSpeckCompiler()

  boolean throwFailure = true
  List<RunListener> listeners = []

  Result runRequest(Request request) {
    def junitCore = new JUnitCore()
    listeners.each { junitCore.addListener(it) }

    def result = junitCore.run(request)
    if (throwFailure && result.failureCount > 0) throw result.failures[0].exception

    result
  }

  Result runClasses(List classes) {
    runRequest(Request.classes(classes as Class[]))
  }

  Result runClass(Class clazz) {
    runRequest(Request.aClass(clazz))
  }

  Result run(String source) {
    runClasses(compiler.compile(source))
  }

  Result runWithImports(String source) {
    runClasses(compiler.compileWithImports(source))
  }

  Result runSpeckBody(String source) {
    runClass(compiler.compileSpeckBody(source))
  }

  Result runFeatureBody(String source) {
    runClass(compiler.compileFeatureBody(source))
  }
}