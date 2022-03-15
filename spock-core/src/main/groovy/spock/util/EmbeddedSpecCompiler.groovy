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

import org.spockframework.runtime.SpecUtil
import org.spockframework.util.*
import spock.lang.Specification

import org.codehaus.groovy.control.*
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.runtime.StringGroovyMethods
import org.intellij.lang.annotations.Language
import org.opentest4j.MultipleFailuresError

/**
 * Utility class that allows to compile (fragments of) specs programmatically.
 * Mainly intended for spec'ing Spock itself.
 *
 * @author Peter Niederwieser
 */
@NotThreadSafe
class EmbeddedSpecCompiler {
  final ImportCustomizer importCustomizer = new ImportCustomizer()
  final CompilerConfiguration compilerConfigurationWithImports = new CompilerConfiguration().tap {
    it.addCompilationCustomizers(importCustomizer)
  }
  final GroovyClassLoader loaderWithImports = new GroovyClassLoader(getClass().classLoader, compilerConfigurationWithImports)
  final GroovyClassLoader loader = new GroovyClassLoader(getClass().classLoader)

  boolean unwrapCompileException = true


  void addPackageImport(String pkg) {
    importCustomizer.addStarImports(pkg)
  }

  void addPackageImport(Package pkg) {
    addPackageImport(pkg.name)
  }

  void addClassImport(String className) {
    importCustomizer.addImports(className)
  }

  void addClassImport(Class<?> clazz) {
    def importName = clazz.name
    if (clazz.memberClass) {
      importName = importName.reverse().replaceFirst(/\$/, ".").reverse()
    }
    addClassImport(importName)
  }

  void addClassMemberImport(String className) {
    importCustomizer.addStaticStars(className)
  }

  void addClassMemberImport(Class<?> clazz) {
    addClassMemberImport(clazz.name)
  }

  /**
   * Compiles the given source code, and returns all Spock specifications
   * contained therein (but not other classes).
   */
  List<Class> compile(@Language('Groovy') String source) {
    doCompile(source, loader)
  }

  List<Class> compileWithImports(@Language('Groovy') String source,
                                 String packageDeclaration = "package apackage;") {
    addPackageImport(Specification.package)
    // one-liner keeps line numbers intact
    doCompile("$packageDeclaration  ${source.trim()}", loaderWithImports)
  }

  Class compileSpecBody(@Language(value = 'Groovy', prefix = 'class ASpec extends spock.lang.Specification { ', suffix = '\n }')
                          String source) {
    // one-liner keeps line numbers intact; newline safeguards against source ending in a line comment
    compileWithImports("class ASpec extends Specification { ${source.trim() + '\n'} }")[0]
  }

  Class compileFeatureBody(@Language(value = 'Groovy', prefix = "def 'a feature'() { ", suffix = '\n }')
                             String source) {
    // one-liner keeps line numbers intact; newline safeguards against source ending in a line comment
    compileSpecBody "def 'a feature'() { ${source.trim() + '\n'} }"
  }

  @Beta
  TranspileResult transpileWithImports(@Language('Groovy') String source,
                                       Set showSet = EnumSet.of(Show.ANNOTATIONS, Show.CLASS, Show.METHODS, Show.FIELDS, Show.OBJECT_INITIALIZERS, Show.PROPERTIES),
                                       CompilePhase phase = CompilePhase.SEMANTIC_ANALYSIS,
                                       String packageDeclaration = "package apackage;") {
    addPackageImport(Specification.package)
    // one-liner keeps line numbers intact
    doTranspile("$packageDeclaration ${source.trim()}", showSet, phase, loaderWithImports, compilerConfigurationWithImports)
  }

  @Beta
  TranspileResult transpileSpecBody(@Language(value = 'Groovy', prefix = 'class ASpec extends spock.lang.Specification { ', suffix = '\n }')
                                   String source,
                                    Set showSet = EnumSet.of(Show.ANNOTATIONS, Show.METHODS, Show.FIELDS, Show.OBJECT_INITIALIZERS, Show.PROPERTIES),
                                    CompilePhase phase = CompilePhase.SEMANTIC_ANALYSIS) {
    // one-liner keeps line numbers intact; newline safeguards against source ending in a line comment
    transpileWithImports("class ASpec extends Specification { ${source.trim() + '\n'} }", showSet, phase)
  }

  @Beta
  TranspileResult transpileFeatureBody(@Language(value = 'Groovy', prefix = "def 'a feature'() { ", suffix = '\n }')
                                      String source,
                                       Set showSet = EnumSet.of(Show.ANNOTATIONS, Show.METHODS),
                                       CompilePhase phase = CompilePhase.SEMANTIC_ANALYSIS) {
    // one-liner keeps line numbers intact; newline safeguards against source ending in a line comment
    transpileSpecBody("def 'a feature'() { ${source.trim() + '\n'} }", showSet, phase)
  }

  @Beta
  TranspileResult transpile(@Language('Groovy') String source, Set showSet = Show.all(), CompilePhase phase = CompilePhase.SEMANTIC_ANALYSIS) {
    doTranspile(source, showSet, phase, loader)
  }

  private TranspileResult doTranspile(@Language('Groovy') String source, Set showSet, CompilePhase phase, GroovyClassLoader gcl, CompilerConfiguration config = null) {
    gcl.clearCache()
    TranspileResult ast = new SourceToAstNodeAndSourceTranspiler().compileScript(source, phase.phaseNumber, showSet, gcl, config)
    // normalize result
    String sourceResult = ast.source
    // Java 15 introduces `stripIndent` with a different behavior, so use explicit method call
    sourceResult = StringGroovyMethods.stripIndent((CharSequence)sourceResult)
    sourceResult = sourceResult.trim()
    return new TranspileResult(sourceResult, ast.nodeCaptures)
  }

  private List<Class> doCompile(@Language('Groovy') String source, GroovyClassLoader gcl) {
    gcl.clearCache()

    try {
      gcl.parseClass(source.trim())
    } catch (MultipleCompilationErrorsException e) {
      def errors = e.errorCollector.errors
      if (unwrapCompileException && errors.every { it.hasProperty("cause") })
        if (errors.size() == 1)
          throw errors[0].cause
        else
          throw new MultipleFailuresError("Errors during compile", errors.cause)

      throw e
    }

    gcl.loadedClasses.findAll {
      SpecUtil.isSpec(it)
    } as List
  }
}
