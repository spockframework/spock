package org.spockframework.runtime

import org.spockframework.report.log.ReportLogConfiguration

import org.spockframework.runtime.extension.builtin.*
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
    result == [RunnerConfiguration, ReportLogConfiguration, TempDirConfiguration]
  }
}
