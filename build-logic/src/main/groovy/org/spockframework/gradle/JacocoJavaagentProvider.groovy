package org.spockframework.gradle

import groovy.transform.CompileStatic
import org.gradle.api.Named
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.process.CommandLineArgumentProvider

@CompileStatic
abstract class JacocoJavaagentProvider implements CommandLineArgumentProvider, Named {
  @Internal
  @Override
  String getName() {
    return 'javaagent'
  }

  @Classpath
  abstract RegularFileProperty getJacocoAgent()

  @OutputFile
  abstract RegularFileProperty getExecResultFile()

  @Override
  Iterable<String> asArguments() {
    [
      "-javaagent:${jacocoAgent.get().asFile}=destfile=${execResultFile.get().asFile},dumponexit=false,append=false,jmx=true,includes=org.spockframework.*".toString(),
      "-DJacocoAstDumpTrigger=true"
    ]
  }
}
