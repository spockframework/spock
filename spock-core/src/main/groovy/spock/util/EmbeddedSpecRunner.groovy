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

import org.junit.runner.notification.RunListener
import org.junit.runner.*

import org.spockframework.util.NotThreadSafe
import org.spockframework.runtime.RunContext
import org.spockframework.util.IFunction
import org.spockframework.runtime.ConfigurationScriptLoader

/**
 * Utility class that allows to run (fragments of) specs programmatically.
 * Mainly intended for spec'ing Spock itself.
 *
 * @author Peter Niederwieser
 */
@NotThreadSafe
class EmbeddedSpecRunner {
  private final EmbeddedSpecCompiler compiler = new EmbeddedSpecCompiler(unwrapCompileException: false)

  boolean throwFailure = true

  List<RunListener> listeners = []
  
  Closure configurationScript = null
  List<Class> extensionClasses = []
  boolean inheritParentExtensions = true

  void addImport(Package pkg) {
    compiler.addImport(pkg)
  }

  Result runRequest(Request request) {
    withNewContext {
      doRunRequest(request)
    }
  }

  Result runClasses(List classes) {
    withNewContext {
      doRunRequest(Request.classes(classes as Class[]))
    }
  }

  // it's very important to open a new context BEFORE Request.aClass/classes is invoked
  // this is because Sputnik is already constructed by those methods, and has to pop
  // the correct context from the stack
  Result runClass(Class clazz) {
    withNewContext {
      doRunRequest(Request.aClass(clazz))
    }
  }

  Result run(String source) {
    runClasses(compiler.compile(source))
  }

  Result runWithImports(String source) {
    runClasses(compiler.compileWithImports(source))
  }

  Result runSpecBody(String source) {
    runClass(compiler.compileSpecBody(source))
  }

  Result runFeatureBody(String source) {
    runClass(compiler.compileFeatureBody(source))
  }

  private Result withNewContext(Closure block) {
    def script = configurationScript ?
        new ConfigurationScriptLoader().loadScript(configurationScript) : null
    RunContext.withNewContext(script, extensionClasses, inheritParentExtensions, block as IFunction)
  }

  private Result doRunRequest(Request request) {
    def junitCore = new JUnitCore()
    listeners.each { junitCore.addListener(it) }

    def result = junitCore.run(request)
    if (throwFailure && result.failureCount > 0) throw result.failures[0].exception

    result
  }
}