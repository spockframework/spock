//file:noinspection ConfigurationAvoidance
package org.spockframework.gradle

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class SpockBasePluginSpec extends Specification {

  def 'Compile settings are configured'() {
    setup:
    def project = createProject()

    when:
    def compileJavaTasks = project.tasks.withType(JavaCompile)
    def compileJava = project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME) as JavaCompile

    then:
    compileJavaTasks.every { it.options.encoding == "UTF-8" }
    compileJava.javaCompiler.get().metadata.languageVersion == SpockBasePlugin.COMPILER_VERSION
  }

  def "Jar settings are configured"() {
    setup:
    def project = createProject()

    when:
    def jarTasks = project.tasks.withType(Jar)

    then:
    jarTasks.every { it.reproducibleFileOrder }
    jarTasks.every { !it.preserveFileTimestamps }
    project.tasks.findByName("sourcesJar") != null
    project.tasks.findByName("javadocJar") != null
  }

  private static Project createProject() {
    def result = ProjectBuilder.builder()
      .build()
    result.plugins.apply(SpockBasePlugin)
    return result
  }
}
