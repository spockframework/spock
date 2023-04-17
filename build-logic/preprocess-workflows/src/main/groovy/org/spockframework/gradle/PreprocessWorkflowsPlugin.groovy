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
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.tasks.JavaExec
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService

@CompileStatic
class PreprocessWorkflowsPlugin implements Plugin<Project> {
  void apply(Project project) {
    def kotlinCompilerClasspath = project.configurations.detachedConfiguration(
      project.dependencies.create('org.jetbrains.kotlin:kotlin-compiler:1.8.20'),
      project.dependencies.create('org.jetbrains.kotlin:kotlin-scripting-compiler:1.8.20')
    )
    def kotlinScriptClasspath = project.configurations.detachedConfiguration(
      project.dependencies.create('org.jetbrains.kotlin:kotlin-main-kts:1.8.20') { ModuleDependency it ->
        it.transitive = false
      }
    )

    def preprocessWorkflows = project.tasks.register('preprocessWorkflows')
    project.file('.github/workflows').eachFileMatch(~/.*\.main\.kts$/) { workflowScript ->
      def workflowName = workflowScript.name - ~/\.main\.kts$/
      def pascalCasedWorkflowName = workflowName
        .replaceAll(/-\w/) { String it -> it[1].toUpperCase() }
        .replaceFirst(/^\w/) { String it -> it[0].toUpperCase() }
      def preprocessWorkflow = project.tasks.register("preprocess${pascalCasedWorkflowName}Workflow", JavaExec) {
        it.inputs
          .file(workflowScript)
          .withPropertyName('workflowScript')
        it.outputs
          .file(new File(workflowScript.parent, "${workflowName}.yml"))
          .withPropertyName('workflowFile')

        it.javaLauncher.set project.extensions.getByType(JavaToolchainService).launcherFor {
          it.languageVersion.set(JavaLanguageVersion.of(17))
        }
        it.classpath(kotlinCompilerClasspath)
        it.mainClass.set 'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler'
        it.args('-no-stdlib', '-no-reflect')
        it.args('-classpath', kotlinScriptClasspath.asPath)
        it.args('-script', workflowScript.absolutePath)
      }
      preprocessWorkflows.configure {
        it.dependsOn(preprocessWorkflow)
      }
    }
  }
}
