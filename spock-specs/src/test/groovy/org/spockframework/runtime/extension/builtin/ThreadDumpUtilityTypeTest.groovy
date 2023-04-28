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

import spock.lang.Specification

import java.nio.file.Paths

import static org.spockframework.runtime.extension.builtin.ThreadDumpUtilityType.JCMD
import static org.spockframework.runtime.extension.builtin.ThreadDumpUtilityType.JSTACK

class ThreadDumpUtilityTypeTest extends Specification {

  def 'can locate #utility on Unix'() {
    expect:
    utility.getPath(Paths.get(javaHome)) == Paths.get(utilityPath)

    where:
    utility | javaHome                    | utilityPath
    JSTACK  | '/opt/jdk/oracle-jdk-8'     | '/opt/jdk/oracle-jdk-8/bin/jstack'
    JSTACK  | '/opt/jdk/oracle-jdk-8/jre' | '/opt/jdk/oracle-jdk-8/bin/jstack'
    JCMD    | '/opt/jdk/oracle-jdk-8'     | '/opt/jdk/oracle-jdk-8/bin/jcmd'
    JCMD    | '/opt/jdk/oracle-jdk-8/jre' | '/opt/jdk/oracle-jdk-8/bin/jcmd'
  }

}
