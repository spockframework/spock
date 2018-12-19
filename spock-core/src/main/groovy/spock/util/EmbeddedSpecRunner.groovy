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

import org.spockframework.runtime.*
import org.spockframework.util.*

import java.lang.reflect.Modifier

import org.intellij.lang.annotations.Language
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.testkit.engine.*
import org.junit.runner.*
import org.junit.runner.notification.*

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
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
    // TODO retire
//    withNewContext {
//      doRunRequest(request)
//    }
    new Result()
  }

  Result runClasses(List classes) {
    withNewContext {
      doRunRequest(classes.findAll { !Modifier.isAbstract(it.modifiers) }.collect {selectClass(it)})
    }
  }

  // it's very important to open a new context BEFORE Request.aClass/classes is invoked
  // this is because Sputnik is already constructed by those methods, and has to pop
  // the correct context from the stack
  Result runClass(Class clazz) {
    withNewContext {
      doRunRequest([selectClass(clazz)])
    }
  }

  Result run(@Language('Groovy') String source) {
    runClasses(compiler.compile(source))
  }

  Result runWithImports(@Language('Groovy') String source) {
    runClasses(compiler.compileWithImports(source))
  }

  Result runSpecBody(@Language(value = 'Groovy', prefix = 'class ASpec extends spock.lang.Specification { ', suffix = '\n }')
                     String source) {
    runClass(compiler.compileSpecBody(source))
  }

  Result runFeatureBody(@Language(value = 'Groovy',
    prefix = "class ASpec extends spock.lang.Specification { def 'a feature'() { ", suffix = '\n } }')
                        String source) {
    runClass(compiler.compileFeatureBody(source))
  }

  def <T> T withNewContext(Closure<T> block) {
    def context = RunContext.get()
    def newContextName = context.name + "/EmbeddedSpecRunner"
    def newSpockUserHome = new File(context.spockUserHome, "EmbeddedSpecRunner")
    def script = configurationScript ?
        new ConfigurationScriptLoader(newSpockUserHome).loadClosureBasedScript(configurationScript) : null
    RunContext.withNewContext(newContextName, newSpockUserHome, script,
        extensionClasses, inheritParentExtensions, block as IThrowableFunction)
  }

  private Result doRunRequest(List<DiscoverySelector> selectors) {
    def executionResults = doRunRequestInner(selectors)

    return new ExecutionResultAdapter(executionResults)
  }
  private EngineExecutionResults doRunRequestInner(List<DiscoverySelector> selectors) {
    def executionResults = EngineTestKit
      .engine("spock")
      .selectors(*selectors)
      .execute()
    if (throwFailure) {
      def first = executionResults.tests().executions().failed().stream().findFirst()
      if (first.present) {
        throw first.get().terminationInfo.executionResult.throwable.get()
      }
    }
    return executionResults
  }

  static class ExecutionResultAdapter extends Result {
    private final EngineExecutionResults results

    ExecutionResultAdapter(EngineExecutionResults results) {
      this.results = results
    }

    @Override
    int getFailureCount() {
      return results.tests().failed().count()
    }

    @Override
    int getRunCount() {
      return results.tests().started().count()
    }

    @Override
    int getIgnoreCount() {
      return results.tests().skipped().count()
    }

    @Override
    List<Failure> getFailures() {
      return results.tests().executions().failed()
        .map{ it.terminationInfo.executionResult.throwable.get()}
        .map {new Failure(Description.createSuiteDescription("FAIL"), it)}
    }
  }
}
