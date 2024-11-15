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
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.jetbrains.annotations.VisibleForTesting

@CompileStatic
class SpockBasePlugin implements Plugin<Project> {

  @VisibleForTesting
  public static final JavaLanguageVersion COMPILER_VERSION = JavaLanguageVersion.of(8)

  void apply(Project project) {
    compileTasks(project)
    testTasks(project)
  }

  private static void compileTasks(Project project) {
    project.with {
      def javaToolchains = extensions.getByType(JavaToolchainService)
      tasks.withType(JavaCompile).configureEach { comp ->
        if (comp.name == JavaPlugin.COMPILE_JAVA_TASK_NAME) {
          comp.javaCompiler.set(javaToolchains.compilerFor {
            it.languageVersion.set(COMPILER_VERSION)
          })
        }
        comp.options.encoding = 'UTF-8'
      }
      tasks.withType(GroovyCompile).configureEach {
        it.options.encoding = 'UTF-8'
      }
    }
  }

  private static void testTasks(Project project) {
    project.tasks.withType(Test).configureEach { task ->
      def taskName = task.name.capitalize()
      File configFile = project.file("Spock${taskName}Config.groovy")
      if (configFile.exists()) {
        task.jvmArgumentProviders.add(new SpockConfigArgumentProvider(configFile))
      }
    }
  }
}
