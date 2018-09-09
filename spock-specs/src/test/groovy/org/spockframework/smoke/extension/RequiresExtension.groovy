/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.extension

import org.spockframework.VerifyExecution
import org.spockframework.ExecutionLog

import spock.lang.Specification
import spock.lang.IgnoreIf
import spock.lang.Issue
import spock.lang.Requires

@VerifyExecution
class RequiresExtension extends Specification {
  @Requires({ 1 < 2 })
  def "runs feature if precondition is satisfied"() {
    expect: true
  }

  @Requires({ 1 > 2 })
  def "skips feature if precondition is not satisfied"() {
    expect: true
  }

  @Requires({ os.windows || os.linux || os.macOs || os.solaris || os.other })
  def "provides OS information"() {
    expect: true
  }

  @Requires({ jvm.java5 || jvm.java6 || jvm.java7 || jvm.java8 || jvm.java9 || jvm.java10 || jvm.java11 })
  def "provides JVM information"() {
    expect: true
  }

  @Requires({ !env.containsKey("FOO_BAR_BAZ") })
  def "provides access to environment variables"() {
    expect: true
  }

  @Requires({ sys.containsKey("java.version") })
  def "provides access to system properties"() {
    expect: true
  }

  @Issue("https://github.com/spockframework/spock/issues/535")
  @IgnoreIf({ true })
  @Requires({ true })
  def "allows determinate use of multiple filters" () {
    expect: false
  }

  def verifyExecution(ExecutionLog log) {
    expect:
    log.passed.size() == 5
    log.failed.size() == 0
    log.skipped == ["skips feature if precondition is not satisfied", "allows determinate use of multiple filters"]
  }
}


