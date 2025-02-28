package org.spockframework.gradle

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject

@CompileStatic
@UntrackedTask(because = 'imported files can import other files so inputs are not determinable upfront')
abstract class DetermineImportedFiles extends DefaultTask {
  @InputFile
  abstract RegularFileProperty getMainKtsFile()

  @InputFiles
  abstract ConfigurableFileCollection getKotlinCompilerEmbeddableClasspath()

  @OutputFile
  abstract RegularFileProperty getImportedFiles()

  @Inject
  abstract WorkerExecutor getWorkerExecutor()

  @Inject
  abstract ProjectLayout getLayout()

  @TaskAction
  def determineImportedFiles() {
    workerExecutor.classLoaderIsolation {
      it.classpath.from(kotlinCompilerEmbeddableClasspath)
    }.submit(DetermineImportedFilesWorkAction) {
      it.projectDirectory.set(layout.projectDirectory)
      it.mainKtsFile.set(mainKtsFile)
      it.importedFiles.set(importedFiles)
    }
  }
}
