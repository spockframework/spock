/*
 * Copyright 2009 the original author or authors.
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

package grails.plugin.spock.build.test.report

import org.codehaus.groovy.grails.test.FormattedOutput
import org.codehaus.groovy.grails.test.XMLFormatter
import org.codehaus.groovy.grails.test.PlainFormatter

class ReportFactory {
  final protected formatters = [
      xml: [
          clazz: XMLFormatter,
          filenameGenerator: { "TEST-${it}.xml" }
      ],
      plain: [
          clazz: PlainFormatter,
          filenameGenerator: { "plain/TEST-${it}.txt" }
      ]
  ]

  final protected reportsDir
  final protected formats

  ReportFactory(File reportsDir, List<String> formats) {
    this.reportsDir = reportsDir
    this.formats = formats
  }

  List<FormattedOutput> createReports(String name) {
    formats.collect { createReport(it, name) }
  }

  protected createReport(format, name) {
    def formatter = formatters[format]
    if (formatter) {
      new FormattedOutput(
          new File(reportsDir, formatter.filenameGenerator(name)),
          formatter.clazz.newInstance()
      )
    } else {
      throw new RuntimeException("Unknown formatter type: $format")
    }
  }
}