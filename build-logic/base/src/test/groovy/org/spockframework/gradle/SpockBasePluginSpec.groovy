/*
 *  Copyright 2025 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

//file:noinspection ConfigurationAvoidance
package org.spockframework.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.VersionConstraint
import org.gradle.api.internal.tasks.testing.junitplatform.JUnitPlatformTestFramework
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.compile.GroovyCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.TempDir
import spock.util.io.FileSystemFixture

class SpockBasePluginSpec extends Specification {
  @TempDir
  FileSystemFixture projectDir

  def 'Compile settings are configured'() {
    setup:
    def project = createProject()

    expect:
    def compileJavaTasks = project.tasks.withType(JavaCompile)
    verifyEach(compileJavaTasks) { options.encoding == "UTF-8" }

    and:
    def compileJava = project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME) as JavaCompile
    compileJava.javaCompiler.get().metadata.languageVersion == SpockBasePlugin.COMPILER_VERSION

    and:
    def compileGroovyTasks = project.tasks.withType(GroovyCompile)
    verifyEach(compileGroovyTasks) { options.encoding == "UTF-8" }
  }

  def "Jar settings are configured"() {
    setup:
    def project = createProject()

    expect:
    def jarTasks = project.tasks.withType(Jar)
    jarTasks.size() == 3
    verifyEach(jarTasks) { reproducibleFileOrder }
    verifyEach(jarTasks) { !preserveFileTimestamps }

    and:
    project.tasks.named("sourcesJar", Jar)
    project.tasks.named("javadocJar", Jar)
  }

  @SuppressWarnings('GroovyInArgumentCheck')
  def "test settings are configured"() {
    setup:
    def spockConfig = projectDir.file("SpockTestConfig.groovy")
    spockConfig.text = '// Spock test config stub'

    and:
    def project = createProject()

    expect:
    def testTask = project.tasks.named('test',Test).get()
    SpockConfigArgumentProvider in testTask.jvmArgumentProviders*.getClass()

    and:
    testTask.reports.junitXml.outputLocation.get().asFile == project.file("build/test-results/test/Test-4.0")
    testTask.reports.html.outputLocation.get().asFile == project.file("build/reports/tests/test/Test-4.0")

    and:
    testTask.testFramework.getClass() == JUnitPlatformTestFramework
  }

  private Project createProject() {
    def project = ProjectBuilder.builder()
      .withName("spock-foo")
      .withProjectDir(projectDir.currentPath.toFile())
      .build()


    project.extensions.tap {
      getExtraProperties().set("variant", "4.0")
      add('versionCatalogs', Stub(VersionCatalogsExtension) {
        find('libs') >> Optional.of(Stub(VersionCatalog) {
          findVersion('jacoco') >> Optional.of(Stub(VersionConstraint) {
            requiredVersion >> '0.8.12'
          })
        })
      })
    }

    project.plugins.apply(SpockBasePlugin)
    return project
  }
}
