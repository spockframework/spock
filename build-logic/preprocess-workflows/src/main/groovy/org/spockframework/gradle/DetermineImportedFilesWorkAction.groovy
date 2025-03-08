package org.spockframework.gradle

import groovy.transform.CompileStatic
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
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

import static org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles.JVM_CONFIG_FILES
import static org.jetbrains.kotlin.config.CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY

@CompileStatic
abstract class DetermineImportedFilesWorkAction implements WorkAction<Parameters> {
  @Override
  void execute() {
    def projectDirectory = parameters.projectDirectory.get().asFile
    parameters
      .mainKtsFile
      .get()
      .asFile
      .with { getImportedFiles(it) }
      .collect { projectDirectory.relativePath(it).toString().replace('\\', '/') }
      .unique()
      .sort()
      .join('\n')
      .tap { parameters.importedFiles.get().asFile.text = it }
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
      ?.findAll { it.shortName?.asString() == 'Import' }
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

  static interface Parameters extends WorkParameters {
    DirectoryProperty getProjectDirectory()

    RegularFileProperty getMainKtsFile()

    RegularFileProperty getImportedFiles()
  }
}
