/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.extension.builtin

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

import static org.spockframework.runtime.extension.builtin.ThreadDumpUtilityType.JCMD
import static org.spockframework.runtime.extension.builtin.ThreadDumpUtilityType.JSTACK

class ThreadDumpUtilityTypeTest extends Specification {

  private static final long PID = 42

  @Shared
  @TempDir
  Path tempDir

  def setupSpec() {
    // create expected paths
    def baseUtilPath = tempDir.resolve('bin')
    Files.createDirectories(baseUtilPath)
    ThreadDumpUtilityType.values().each {
      Files.createFile(baseUtilPath.resolve(it.fileName))
    }
  }

  def '#utility utility provides correct command arguments'() {
    given:
    expect:
    utility.getCommand(javaHome, PID) == [utilityPath.toString(), PID.toString()] + additionalArgs

    where:
    utility | javaHome               | additionalArgs
    JSTACK  | tempDir                | []
    JSTACK  | tempDir.resolve('jre') | []
    JCMD    | tempDir                | ['Thread.print']
    JCMD    | tempDir.resolve('jre') | ['Thread.print']

    utilityPath = tempDir.resolve('bin').resolve(utility.fileName)
  }

}
