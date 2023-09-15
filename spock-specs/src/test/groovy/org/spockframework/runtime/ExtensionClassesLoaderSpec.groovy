/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime

import org.spockframework.report.log.ReportLogConfiguration
import org.spockframework.runtime.extension.builtin.IncludeExcludeExtension
import org.spockframework.runtime.extension.builtin.OptimizeRunOrderExtension
import org.spockframework.runtime.extension.builtin.TimeoutConfiguration
import org.spockframework.runtime.extension.builtin.UnrollExtension
import org.spockframework.specs.extension.SnapshotConfig
import org.spockframework.tempdir.TempDirConfiguration
import spock.config.RunnerConfiguration
import spock.lang.Specification

class ExtensionClassesLoaderSpec extends Specification {

  def "loads GlobalExtensions"() {
    when:
    def result = new ExtensionClassesLoader().loadExtensionClassesFromDefaultLocation()

    then:
    result == [IncludeExcludeExtension, OptimizeRunOrderExtension, OrderExtension, UnrollExtension]
  }

  def "loads global ConfigObjects"() {
    when:
    def result = new ExtensionClassesLoader().loadConfigClassesFromDefaultLocation()

    then:
    result == [SnapshotConfig, RunnerConfiguration, ReportLogConfiguration, TempDirConfiguration, TimeoutConfiguration]
  }
}
