/*
 * Copyright 2013 the original author or authors.
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

package org.spockframework.report.log

import spock.lang.Specification

class ReportLogConfigurationSpec extends Specification {
  def configuration = new ReportLogConfiguration()

  def "defaults"() {
    expect:
    with(configuration) {
      !enabled

      logFileDir == null
      logFileName == null
      logFileSuffix == null

      issueNamePrefix == ""
      issueUrlPrefix == ""

      reportServerAddress == null
      reportServerPort == 4242
    }
  }

  def "can use timestamp placeholder in suffix"() {
    configuration.logFileDir = "/foo/bar"
    configuration.logFileName = "baz.log"
    configuration.logFileSuffix = "at-#timestamp"

    expect:
    configuration.logFile ==~ "/foo/bar/baz-at-2\\d\\d\\d-.+\\.log"
  }
}
