package org.spockframework.runtime

import org.spockframework.mock.runtime.MockMakerConfiguration
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
    result == [IncludeExcludeExtension, OptimizeRunOrderExtension, UnrollExtension]
  }

  def "loads global ConfigObjects"() {
    when:
    def result = new ExtensionClassesLoader().loadConfigClassesFromDefaultLocation()

    then:
    result == [SnapshotConfig,
               RunnerConfiguration,
               ReportLogConfiguration,
               TempDirConfiguration,
               TimeoutConfiguration,
               MockMakerConfiguration
    ]
  }
}
