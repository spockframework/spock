/*
 * Copyright 2013 the original author or authors.
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

package org.spockframework.gradle

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.openapi.vfs.local.CoreLocalFileSystem
import org.jetbrains.kotlin.com.intellij.openapi.vfs.local.CoreLocalVirtualFile
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

import static org.jetbrains.kotlin.cli.common.CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY
import static org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles.JVM_CONFIG_FILES

@CompileStatic
class PreprocessWorkflowsPlugin implements Plugin<Project> {
  void apply(Project project) {
    def libs = project.extensions.getByType(VersionCatalogsExtension).find('libs').orElseThrow(AssertionError::new)
    def kotlinCompilerClasspath = project.configurations.detachedConfiguration(
      libs.findLibrary('workflows-kotlin-compiler').orElseThrow(AssertionError::new).get(),
      libs.findLibrary('workflows-kotlin-scriptingCompiler').orElseThrow(AssertionError::new).get()
    )
    def kotlinScriptClasspath = project.configurations.detachedConfiguration(
      libs.findLibrary('workflows-kotlin-mainKts').orElseThrow(AssertionError::new).get()
    ).tap {
      it.transitive = false
    }

    def preprocessWorkflows = project.tasks.register('preprocessWorkflows') {
      it.group = 'github actions'
    }
    project.file('.github/workflows').eachFileMatch(~/.*\.main\.kts$/) { workflowScript ->
      def workflowName = workflowScript.name - ~/\.main\.kts$/
      def pascalCasedWorkflowName = workflowName
        .replaceAll(/-\w/) { String it -> it[1].toUpperCase() }
        .replaceFirst(/^\w/) { String it -> it[0].toUpperCase() }
      def preprocessWorkflow = project.tasks.register("preprocess${pascalCasedWorkflowName}Workflow", JavaExec) {
        it.group = 'github actions'

        it.inputs
          .file(workflowScript)
          .withPropertyName('workflowScript')
        it.inputs
          .files(getImportedFiles(project.file(workflowScript)))
          .withPropertyName("importedFiles")
        it.outputs
          .file(new File(workflowScript.parent, "${workflowName}.yaml"))
          .withPropertyName('workflowFile')

        it.javaLauncher.set project.extensions.getByType(JavaToolchainService).launcherFor {
          it.languageVersion.set(JavaLanguageVersion.of(17))
        }
        it.classpath(kotlinCompilerClasspath)
        it.mainClass.set 'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler'
        it.args('-no-stdlib', '-no-reflect')
        it.args('-classpath', kotlinScriptClasspath.asPath)
        it.args('-script', workflowScript.absolutePath)

        // work-around for https://youtrack.jetbrains.com/issue/KT-42101
        it.systemProperty('kotlin.main.kts.compiled.scripts.cache.dir', '')
      }
      project.pluginManager.withPlugin('io.spring.nohttp') {
        // iff both tasks are run, workflow files should be generated before checkstyle check
        project.tasks.named('checkstyleNohttp') {
          it.mustRunAfter(preprocessWorkflow)
        }
      }
      preprocessWorkflows.configure {
        it.dependsOn(preprocessWorkflow)
      }
    }
  }

  private List<File> getImportedFiles(File workflowScript) {
    if (!workflowScript.file) {
      return []
    }

    return PsiManager
      .getInstance(
        KotlinCoreEnvironment
          .createForProduction(
            Disposer.newDisposable(),
            new CompilerConfiguration().tap {
              it.put(MESSAGE_COLLECTOR_KEY, MessageCollector.@Companion.NONE)
            },
            JVM_CONFIG_FILES
          )
          .project
      )
      .findFile(
        new CoreLocalVirtualFile(
          new CoreLocalFileSystem(),
          workflowScript.toPath()
        )
      )
      .with { it as KtFile }
      .fileAnnotationList
      ?.annotationEntries
      ?.findAll { it.shortName?.asString() == "Import" }
      *.valueArgumentList
      ?.collectMany { it?.arguments ?: [] }
      *.argumentExpression
      ?.findAll { it instanceof KtStringTemplateExpression }
      ?.collect { it as KtStringTemplateExpression }
      *.entries
      *.first()
      ?.findAll { it instanceof KtLiteralStringTemplateEntry }
      ?.collect { it as KtLiteralStringTemplateEntry }
      ?.collect { new File(workflowScript.parentFile, it.text) }
      ?.collectMany { getImportedFiles(it) + it }
      ?: []
  }
}
