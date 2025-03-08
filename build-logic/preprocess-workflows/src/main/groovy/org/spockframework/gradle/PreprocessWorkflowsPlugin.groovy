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
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService

@CompileStatic
class PreprocessWorkflowsPlugin implements Plugin<Project> {
  void apply(Project project) {
    def libs = project.extensions.getByType(VersionCatalogsExtension).find('libs').orElseThrow(AssertionError::new)
    def kotlinCompilerEmbeddableClasspath = project.configurations.detachedConfiguration(
      libs.findLibrary('workflows-kotlin-compilerEmbeddable').orElseThrow(AssertionError::new).get(),
    )
    def kotlinCompilerClasspath = project.configurations.detachedConfiguration(
      libs.findLibrary('workflows-kotlin-compiler').orElseThrow(AssertionError::new).get(),
      libs.findLibrary('workflows-kotlin-scriptingCompiler').orElseThrow(AssertionError::new).get()
    )
    def mainKtsClasspath = project.configurations.detachedConfiguration(
      libs.findLibrary('workflows-kotlin-mainKts').orElseThrow(AssertionError::new).get()
    ).tap {
      it.transitive = false
    }

    def preprocessWorkflows = project.tasks.register('preprocessWorkflows') {
      it.group = 'github workflows'
    }
    project.file('.github/workflows').eachFileMatch(~/.*\.main\.kts$/) { workflowScript ->
      def workflowName = workflowScript.name - ~/\.main\.kts$/
      def pascalCasedWorkflowName = workflowName
        .replaceAll(/-\w/) { String it -> it[1].toUpperCase() }
        .replaceFirst(/^\w/) { String it -> it[0].toUpperCase() }
      def determineImportedFiles = project.tasks.register("determineImportedFilesFor${pascalCasedWorkflowName}Workflow", DetermineImportedFiles) {
        it.mainKtsFile.set(workflowScript)
        it.importedFiles.set(project.layout.buildDirectory.file("importedFilesFor${pascalCasedWorkflowName}Workflow.txt"))
        it.kotlinCompilerEmbeddableClasspath.from(kotlinCompilerEmbeddableClasspath)
      }
      def preprocessWorkflow = project.tasks.register("preprocess${pascalCasedWorkflowName}Workflow", PreprocessGithubWorkflow) {
        it.workflowScript.set(workflowScript)
        it.importedFiles.from(determineImportedFiles.flatMap { it.importedFiles }.map { it.asFile.readLines() })
        it.kotlinCompilerClasspath.from(kotlinCompilerClasspath)
        it.mainKtsClasspath.from(mainKtsClasspath)
        it.javaLauncher.set(project.extensions.getByType(JavaToolchainService).launcherFor {
          it.languageVersion.set(JavaLanguageVersion.of(17))
        })
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
}
