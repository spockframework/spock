package org.spockframework.gradle

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import org.gradle.api.Named
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.process.CommandLineArgumentProvider

@CompileStatic
@TupleConstructor
class SpockConfigArgumentProvider implements CommandLineArgumentProvider, Named {
  @InputFile
  @PathSensitive(PathSensitivity.RELATIVE)
  final File configFile

  @Internal
  @Override
  String getName() {
    return 'spock.configuration'
  }

  @Override
  Iterable<String> asArguments() {
    ["-Dspock.configuration=${configFile.absolutePath}".toString()]
  }
}
