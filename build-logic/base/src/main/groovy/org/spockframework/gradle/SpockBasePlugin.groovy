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
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.diagnostics.DependencyInsightReportTask
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.jetbrains.annotations.VisibleForTesting

import java.time.Duration

@CompileStatic
class SpockBasePlugin implements Plugin<Project> {

  @VisibleForTesting
  public static final JavaLanguageVersion COMPILER_VERSION = JavaLanguageVersion.of(8)

  void apply(Project project) {
    applyPlugins(project)
    compileTasks(project)
    jarTasks(project)
    testTasks(project)
    jacoco(project)
    dependencyInsight(project)
  }

  static void applyPlugins(Project project) {
    def plugins = project.plugins
    plugins.apply("java-library")
    plugins.apply("groovy")
    plugins.apply("jacoco")
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

  private static void jarTasks(Project project) {
    project.tasks.withType(Jar).configureEach { jar ->
      /*
       * Ensure the jar can be built in a reproducible manner, This shall prevent build cache misses, when different variants are tested.
       */
      jar.preserveFileTimestamps = false
      jar.reproducibleFileOrder = true
    }

    def sourceSets = project.extensions.getByType(SourceSetContainer)
    project.tasks.register("sourcesJar", Jar) {
      it.archiveClassifier.set("sources")
      it.from(sourceSets.named("main").map { it.allSource })
    }

    project.tasks.register("javadocJar", Jar) {
      it.archiveClassifier.set("javadoc")
      it.from(project.tasks.named("javadoc"))
    }
  }

  private static void testTasks(Project project) {
    project.tasks.withType(Test).configureEach { task ->
      task.useJUnitPlatform()
      def taskName = task.name.capitalize()
      def variant = project.rootProject.extensions.getByType(ExtraPropertiesExtension)
        .get("variant")

      def junitXml = task.reports.junitXml
      junitXml.outputLocation.set(project.file("${junitXml.outputLocation.get()}/$taskName-$variant"))
      def html = task.reports.html
      html.outputLocation.set(project.file("${html.outputLocation.get()}/$taskName-$variant"))

      // As a generous general timeout, instead of the 6h of GHA.
      // But only on CI or longer needing debug sessions get killed by the timeout.
      boolean isCiServer = System.getenv("CI") || System.getenv("GITHUB_ACTIONS")
      if (isCiServer) {
        task.timeout.set(Duration.ofMinutes(15))
      }

      File configFile = project.file("Spock${taskName}Config.groovy")
      if (configFile.exists()) {
        task.jvmArgumentProviders.add(new SpockConfigArgumentProvider(configFile))
      }
    }
  }

  private static void jacoco(Project project) {
    // For tests, we have to handle the case where version catalogs are not present
    project.rootProject.extensions.findByType(VersionCatalogsExtension)?.find("libs")
      ?.flatMap { it.findVersion("jacoco") }
      ?.ifPresent { version ->
        project.extensions.getByType(JacocoPluginExtension).toolVersion = version.toString()
      }
  }

  private static void dependencyInsight(Project project) {
    project.tasks.register("allDependencyInsight", DependencyInsightReportTask)
  }
}
