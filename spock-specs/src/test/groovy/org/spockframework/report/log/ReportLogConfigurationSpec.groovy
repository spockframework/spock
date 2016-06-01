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
import spock.lang.Unroll
import spock.util.environment.RestoreSystemProperties

@RestoreSystemProperties
class ReportLogConfigurationSpec extends Specification {

  def "defaults"() {
    given:
    ['spock.logEnabled', 'spock.logFileDir', 'spock.logFileName', 'spock.logFileSuffix'].each {
      System.clearProperty(it)
    }
    def configuration = new ReportLogConfiguration()

    expect:
    with(configuration) {
      !enabled

      logFileDir == System.getProperty('spock.logFileDir')
      logFileName == System.getProperty('spock.logFileName')
      logFileSuffix == System.getProperty('spock.logFileSuffix')

      issueNamePrefix == ""
      issueUrlPrefix == ""

      reportServerAddress == null
      reportServerPort == 4242
    }
  }

  def "can use timestamp placeholder in suffix"() {
    given:
    def configuration = new ReportLogConfiguration()
    configuration.logFileDir = 'foo/bar'
    configuration.logFileName = 'baz.log'
    configuration.logFileSuffix = 'at-#timestamp'
    def sep = System.getProperty('file.separator')

    expect:
    configuration.logFile.path.replace(sep, "/") ==~ "foo/bar/baz-at-2\\d\\d\\d-.+\\.log"
  }

  @Unroll
  def "handles spock.logEnabled #description"(boolean set, String value, boolean enabled, String description) {
    given:
    if (set) {
      System.setProperty('spock.logEnabled', value)
    } else {
      System.clearProperty('spock.logEnabled')
    }

    def configuration = new ReportLogConfiguration()

    expect:
    configuration.enabled == enabled

    where:
    set   | value     || enabled
    true  | 'false'   || false
    true  | 'true'    || true
    true  | 'foo'     || false
    false | 'ignored' || false

    description = set ? "set to '$value'" : "unset"
  }
}
