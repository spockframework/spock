package org.spockframework.gradle

import groovy.transform.CompileStatic
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.jetbrains.kotlin.cli.jvm.compiler.IdeaStandaloneExecutionSetup
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreApplicationEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreApplicationEnvironmentMode.Production
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreProjectEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.parsing.KotlinParserDefinition
import org.jetbrains.kotlin.com.intellij.openapi.vfs.local.CoreLocalFileSystem
import org.jetbrains.kotlin.com.intellij.openapi.vfs.local.CoreLocalVirtualFile
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

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

    def disposable = Disposer.newDisposable()
    IdeaStandaloneExecutionSetup.INSTANCE.doSetup()
    return PsiManager
      .getInstance(
        new KotlinCoreProjectEnvironment(
          disposable,
          KotlinCoreApplicationEnvironment.@Companion.create(
            disposable,
            Production.INSTANCE
          ).tap {
            registerParserDefinition(new KotlinParserDefinition())
            registerFileType(KotlinFileType.INSTANCE, 'kts')
          }
        ).project
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
