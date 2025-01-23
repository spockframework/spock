package org.spockframework.gradle

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject

@CompileStatic
abstract class PreprocessGithubWorkflow extends DefaultTask {
  @InputFile
  abstract RegularFileProperty getWorkflowScript()

  @InputFiles
  abstract ConfigurableFileCollection getImportedFiles()

  @InputFiles
  abstract ConfigurableFileCollection getKotlinCompilerClasspath()

  @InputFiles
  abstract ConfigurableFileCollection getMainKtsClasspath()

  @Nested
  abstract Property<JavaLauncher> getJavaLauncher()

  @OutputFile
  Provider<File> getWorkflowFile() {
    workflowScript.map {
      def workflowScript = it.asFile
      workflowScript.toPath().resolveSibling("${workflowScript.name - ~/\.main\.kts$/}.yaml").toFile()
    }
  }

  @Inject
  abstract WorkerExecutor getWorkerExecutor()

  PreprocessGithubWorkflow() {
    group = 'github workflows'
  }

  @TaskAction
  def determineImportedFiles() {
    workerExecutor.noIsolation().submit(PreprocessGithubWorkflowWorkAction) {
      it.workflowScript.set(workflowScript)
      it.kotlinCompilerClasspath.from(kotlinCompilerClasspath)
      it.mainKtsClasspath.from(mainKtsClasspath)
      it.javaExecutable.set(javaLauncher.map { it.executablePath })
    }
  }
}
