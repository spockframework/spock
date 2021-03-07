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
import spock.lang.Specification

import java.util.regex.Pattern

import org.codehaus.groovy.control.*
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
  static final FILENAME_PATTERN = Pattern.compile(/(?:(?<=filename = 'script)(\d+)(?=.groovy')|(?<=\w@)([a-z\d]+))/)
  static final TRAILING_SPACES = Pattern.compile(/ +(?=\n)/)
  static final Class<?> AST_TRANSFORMER = ReflectionUtil.loadClassIfAvailable(GroovyReleaseInfo.version < VersionNumber.parse("3") ? 'groovy.inspect.swingui.AstNodeToScriptAdapter' : 'groovy.console.ui.AstNodeToScriptAdapter')
  final GroovyClassLoader loader = new GroovyClassLoader(getClass().classLoader)

  boolean unwrapCompileException = true

  String imports = ""

  void addPackageImport(String pkg) {
    imports += "import $pkg.*;"
  }

  void addPackageImport(Package pkg) {
    addPackageImport(pkg.name)
  }

  void addClassImport(String className) {
    imports += "import $className;"
  }

  void addClassImport(Class<?> clazz) {
    def importName = clazz.name
    if (clazz.memberClass) {
      importName = importName.reverse().replaceFirst(/\$/, ".").reverse()
    }
    addClassImport(importName)
  }

  void addClassMemberImport(String className) {
    imports += "import static $className.*;"
  }

  void addClassMemberImport(Class<?> clazz) {
    addClassMemberImport(clazz.name)
  }

  /**
   * Compiles the given source code, and returns all Spock specifications
   * contained therein (but not other classes).
   */
  List<Class> compile(@Language('Groovy') String source) {
    doCompile(imports + source)
  }

  List<Class> compileWithImports(@Language('Groovy') String source) {
    addPackageImport(Specification.package )
    // one-liner keeps line numbers intact
    doCompile "package apackage; $imports ${source.trim()}"
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

  String astToSourceWithImports(@Language('Groovy') String source, CompilePhase phase = CompilePhase.SEMANTIC_ANALYSIS) {
    addPackageImport(Specification.package )
    // one-liner keeps line numbers intact
    doAstToSource("package apackage; $imports ${source.trim()}", phase)
  }

  String astToSourceSpecBody(@Language(value = 'Groovy', prefix = 'class ASpec extends spock.lang.Specification { ', suffix = '\n }')
                        String source, CompilePhase phase = CompilePhase.SEMANTIC_ANALYSIS) {
    // one-liner keeps line numbers intact; newline safeguards against source ending in a line comment
    astToSourceWithImports("class ASpec extends Specification { ${source.trim() + '\n'} }", phase)
  }

  String astToSourceFeatureBody(@Language(value = 'Groovy', prefix = "def 'a feature'() { ", suffix = '\n }')
                           String source, CompilePhase phase = CompilePhase.SEMANTIC_ANALYSIS) {
    // one-liner keeps line numbers intact; newline safeguards against source ending in a line comment
    astToSourceSpecBody ("def 'a feature'() { ${source.trim() + '\n'} }", phase)
  }

  String astToSource(@Language('Groovy') String source, CompilePhase phase = CompilePhase.SEMANTIC_ANALYSIS) {
    doAstToSource(imports + source, phase)
  }

  String doAstToSource(@Language('Groovy') String source, CompilePhase phase) {
    if (AST_TRANSFORMER == null) {
      throw new SpockException("To use any astToSource* method you need to add groovy-console as dependency.")
    }
    String result = AST_TRANSFORMER.getConstructor().newInstance().compileToScript(source, phase.phaseNumber)
    // normalize result
    result = FILENAME_PATTERN.matcher(result).replaceAll("XXXXX")
    result = TRAILING_SPACES.matcher(result).replaceAll("")
    result = result.trim()
    return result
  }


  private List<Class> doCompile(@Language('Groovy') String source) {
    loader.clearCache()

    try {
    loader.parseClass(source.trim())
    } catch (MultipleCompilationErrorsException e) {
      def errors = e.errorCollector.errors
      if (unwrapCompileException && errors.every { it.hasProperty("cause") })
        if (errors.size() == 1)
          throw errors[0].cause
        else
          throw new MultipleFailuresError("Errors during compile",errors.cause)

      throw e
    }

    loader.loadedClasses.findAll {
      SpecUtil.isSpec(it)
    } as List
  }
}
