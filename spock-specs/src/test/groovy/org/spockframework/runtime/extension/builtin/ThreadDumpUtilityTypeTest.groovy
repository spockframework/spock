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
