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

import org.intellij.lang.annotations.Language
import org.junit.runner.JUnitCore
import org.junit.runner.Request
import org.junit.runner.Result
import org.junit.runner.notification.RunListener
import org.spockframework.runtime.ConfigurationScriptLoader
import org.spockframework.runtime.RunContext
import org.spockframework.util.IThrowableFunction
import org.spockframework.util.NotThreadSafe

import java.lang.reflect.Modifier

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

  void addPackageImport(String pkg) {
    compiler.addPackageImport(pkg)
  }

  void addPackageImport(Package pkg) {
    compiler.addPackageImport(pkg)
  }

  void addClassImport(String className) {
    compiler.addClassImport(className)
  }

  void addClassImport(Class<?> clazz) {
    compiler.addClassImport(clazz)
  }

  void addClassMemberImport(String className) {
    compiler.addClassMemberImport(className)
  }

  void addClassMemberImport(Class<?> clazz) {
    compiler.addClassMemberImport(clazz)
  }

  Result runRequest(Request request) {
    withNewContext {
      doRunRequest(request)
    }
  }

  Result runClasses(List classes) {
    withNewContext {
      doRunRequest(Request.classes(classes.findAll { !Modifier.isAbstract(it.modifiers) } as Class[]))
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

  Result runSpecBody(@Language(value = 'Groovy', prefix = 'class ASpec extends spock.lang.Specification { ', suffix = '\n}')
                     String source) {
    runClass(compiler.compileSpecBody(source))
  }

  Result runFeatureBody(@Language(value = 'Groovy', prefix = "def 'a feature'() { ", suffix = '\n}')
                        String source) {
    runClass(compiler.compileFeatureBody(source))
  }

  def withNewContext(Closure block) {
    def context = RunContext.get()
    def newContextName = context.name + "/EmbeddedSpecRunner"
    def newSpockUserHome = new File(context.spockUserHome, "EmbeddedSpecRunner")
    def script = configurationScript ?
        new ConfigurationScriptLoader(newSpockUserHome).loadClosureBasedScript(configurationScript) : null
    RunContext.withNewContext(newContextName, newSpockUserHome, script,
        extensionClasses, inheritParentExtensions, block as IThrowableFunction)
  }

  private Result doRunRequest(Request request) {
    def junitCore = new JUnitCore()
    listeners.each { junitCore.addListener(it) }

    def result = junitCore.run(request)
    if (throwFailure && result.failureCount > 0) throw result.failures[0].exception

    result
  }
}
