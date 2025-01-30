package org.spockframework.gradle

import groovy.transform.CompileStatic
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

import javax.inject.Inject

@CompileStatic
abstract class PreprocessGithubWorkflowWorkAction implements WorkAction<Parameters> {
  @Inject
  abstract ExecOperations getExecOperations()

  @Override
  void execute() {
    // work-around for https://youtrack.jetbrains.com/issue/KT-74830
    Exception lastException = null
    for (i in 1..5) {
      try {
        execOperations.javaexec {
          it.executable = parameters.javaExecutable.get().asFile.absolutePath
          it.classpath(parameters.kotlinCompilerClasspath)
          it.mainClass.set('org.jetbrains.kotlin.cli.jvm.K2JVMCompiler')
          it.args('-no-stdlib', '-no-reflect')
          it.args('-classpath', parameters.mainKtsClasspath.asPath)
          it.args('-script', parameters.workflowScript.get().asFile.absolutePath)

          // work-around for https://youtrack.jetbrains.com/issue/KT-42101
          it.systemProperty('kotlin.main.kts.compiled.scripts.cache.dir', '')
        }
        return
      } catch (Exception e) {
        lastException = e
      }
    }
    throw lastException
  }

  static interface Parameters extends WorkParameters {
    RegularFileProperty getWorkflowScript()

    ConfigurableFileCollection getKotlinCompilerClasspath()

    ConfigurableFileCollection getMainKtsClasspath()

    RegularFileProperty getJavaExecutable()
  }
}
