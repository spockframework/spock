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
