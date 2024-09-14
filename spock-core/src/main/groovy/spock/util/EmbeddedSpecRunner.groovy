/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spock.util

import org.junit.platform.engine.Filter
import org.spockframework.runtime.*
import org.spockframework.util.*

import java.lang.reflect.Modifier
import java.util.stream.Collectors

import groovy.transform.TupleConstructor
import org.intellij.lang.annotations.Language
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.listeners.TestExecutionSummary
import org.junit.platform.testkit.engine.*

import static java.util.Arrays.asList
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass

/**
 * Utility class that allows to run (fragments of) specs programmatically.
 * Mainly intended for spec'ing Spock itself.
 *
 * @author Peter Niederwieser
 */
@NotThreadSafe
class EmbeddedSpecRunner {
  private final EmbeddedSpecCompiler compiler

  boolean throwFailure = true

  Closure configurationScript = null
  List<Class> extensionClasses = []
  List<Class> configClasses = []
  boolean inheritParentExtensions = true

  /**
   * Creates a new {@link EmbeddedSpecRunner} with a default {@link EmbeddedSpecCompiler}.
   */
  EmbeddedSpecRunner() {
    this(new EmbeddedSpecCompiler())
    this.compiler.unwrapCompileException = false
  }

  /**
   * Creates a new {@link EmbeddedSpecRunner} with a custom classloader to load imports of the spec.
   * Note: The classloader need to be able to load Spock classes.
   *
   * @param classLoader the ClassLoader to use to load the spec imports
   * @since 2.4
   */
  @Beta
  EmbeddedSpecRunner(ClassLoader classLoader) {
    this(new EmbeddedSpecCompiler(classLoader))
    this.compiler.unwrapCompileException = false
  }

  /**
   * Creates a new {@link EmbeddedSpecRunner} with a custom {@link EmbeddedSpecCompiler}.
   *
   * @param compiler the compiler to use
   * @since 2.4
   */
  @Beta
  EmbeddedSpecRunner(EmbeddedSpecCompiler compiler) {
    this.compiler = Objects.requireNonNull(compiler)
  }

  void configurationScript(Closure configurationScript) {
    this.configurationScript = configurationScript
  }

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

  SummarizedEngineExecutionResults runWithSelectors(DiscoverySelector... selectors) {
    runWithSelectors(asList(selectors))
  }

  SummarizedEngineExecutionResults runWithSelectors(List<DiscoverySelector> selectors) {
    withNewContext {
      doRunRequest(selectors)
    }
  }

  SummarizedEngineExecutionResults runClasses(List<Class<?>> classes) {
    withNewContext {
      doRunRequest(classes.findAll { !Modifier.isAbstract(it.modifiers) }.collect {selectClass(it)})
    }
  }

  SummarizedEngineExecutionResults runClass(Class clazz, Filter<?>... filters) {
    withNewContext {
      doRunRequest([selectClass(clazz)], asList(filters))
    }
  }

  SummarizedEngineExecutionResults run(@Language('Groovy') String source) {
    runClasses(compiler.compile(source))
  }

  SummarizedEngineExecutionResults runWithImports(@Language('Groovy') String source) {
    runClasses(compiler.compileWithImports(source))
  }

  SummarizedEngineExecutionResults runSpecBody(@Language(value = 'Groovy', prefix = 'class ASpec extends spock.lang.Specification { ', suffix = '\n }')
                     String source) {
    runClass(compiler.compileSpecBody(source))
  }

  SummarizedEngineExecutionResults runFeatureBody(@Language(value = 'Groovy',
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
    (T)RunContext.withNewContext(newContextName, newSpockUserHome, script,
        extensionClasses, configClasses, inheritParentExtensions, block as IThrowableFunction)
  }

  private SummarizedEngineExecutionResults doRunRequest(List<DiscoverySelector> selectors, List<Filter<?>> filters = []) {
    def executionResults = doRunRequestInner(selectors, filters)

    return new SummarizedEngineExecutionResults(executionResults)
  }

  private EngineExecutionResults doRunRequestInner(List<DiscoverySelector> selectors, List<Filter<?>> filters = []) {
    def executionResults = EngineTestKit
      .engine("spock")
      .selectors(*selectors)
      .filters(*filters)
      .execute()
    if (throwFailure) {
      def first = executionResults.allEvents().executions().failed().stream().findFirst()
      if (first.present) {
        throw first.get().terminationInfo.executionResult.throwable.get()
      }
    }
    return executionResults
  }

  static class SummarizedEngineExecutionResults implements TestExecutionSummary {
    @Delegate
    private final EngineExecutionResults results

    SummarizedEngineExecutionResults(EngineExecutionResults results) {
      this.results = results
    }

    @Deprecated
    int getFailureCount() {
      return results.testEvents().failed().count()
    }

    @Deprecated
    int getRunCount() {
      return results.testEvents().started().count()
    }

    @Deprecated
    int getIgnoreCount() {
      return results.testEvents().skipped().count()
    }

    @Override
    long getTimeStarted() {
      return results.allEvents().started().stream().findFirst().map{it.timestamp.toEpochMilli()}.orElseGet {0}
    }

    @Override
    long getTimeFinished() {
      return results.allEvents().finished().stream()
        .reduce{first, second -> second} // fancy for .last()
        .map{it.timestamp.toEpochMilli()}.orElseGet {0}
    }

    long getDynamicallyRegisteredCount() {
      return results.allEvents().dynamicallyRegistered().count()
    }

    long getTotalStartedCount() {
      return results.allEvents().started().count()
    }

    long getTotalSkippedCount() {
      return results.allEvents().skipped().count()
    }

    long getTotalAbortedCount() {
      return results.allEvents().aborted().count()
    }

    long getTotalSucceededCount() {
      return results.allEvents().succeeded().count()
    }

    @Override
    long getTotalFailureCount() {
      return results.allEvents().failed().count()
    }

    @Override
    long getContainersFoundCount() {
      return 0
    }

    @Override
    long getContainersStartedCount() {
      return results.containerEvents().started().count()
    }

    @Override
    long getContainersSkippedCount() {
      return results.containerEvents().skipped().count()
    }

    @Override
    long getContainersAbortedCount() {
      return results.containerEvents().aborted().count()
    }

    @Override
    long getContainersSucceededCount() {
      return results.containerEvents().succeeded().count()
    }

    @Override
    long getContainersFailedCount() {
      return results.containerEvents().failed().count()
    }

    @Override
    long getTestsFoundCount() {
      return 0
    }

    @Override
    long getTestsStartedCount() {
      return results.testEvents().started().count()
    }

    @Override
    long getTestsSkippedCount() {
      return results.testEvents().skipped().count()
    }

    @Override
    long getTestsAbortedCount() {
      return results.testEvents().aborted().count()
    }

    @Override
    long getTestsSucceededCount() {
      return results.testEvents().succeeded().count()
    }

    @Override
    long getTestsFailedCount() {
      return results.testEvents().failed().count()
    }

    @Override
    void printTo(PrintWriter writer) {
      throw new UnsupportedOperationException('Not Implemented')
    }

    @Override
    void printFailuresTo(PrintWriter writer) {
      throw new UnsupportedOperationException('Not Implemented')
    }

    @Override
    List<Failure> getFailures() {
      return results.allEvents().executions().failed()
        .map{ it.terminationInfo.executionResult.throwable.get()}
        .map {new XFailure(it)}
        .collect(Collectors.toList())
    }
  }

  @TupleConstructor
  static class XFailure implements TestExecutionSummary.Failure {
    Throwable exception

    @Override
    TestIdentifier getTestIdentifier() {
      throw new UnsupportedOperationException('Not Implemented')
    }
  }
}
